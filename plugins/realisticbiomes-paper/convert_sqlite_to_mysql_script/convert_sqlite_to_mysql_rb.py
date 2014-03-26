# #!/usr/bin/env python3
#
# script to take a sqlite3 database, and create the appropriate tables in a mysql database
# and move all of the data over to mysql for realistic biomes
#
# written by Mark Grandi, June 11, 2013
#

import argparse, sys, traceback, sqlite3, os.path

try:
    import myconnpy_database
except ImportError as e:
    sys.exit("Failed to import 'myconn_database', it should of been in the same directory as this script.... error: {}".foramt(e))
try:
    import yaml
except ImportError as e:
   sys.exit("you need PyYAML! get it from https://pypi.python.org/pypi/PyYAML   error: {}".format(e))
    


def convertSqliteToMysql(args):
    ''' does the work of converting the sqlite database to mysql
    @param args - the argparse namespace object we get from parse_args()
    '''


    # sqlite3 db is already connected through argparse
    sqliteDb = args.sqliteDb
    #sqliteDb.row_factory = sqlite3.Row

    mysqlDb = None
    # see if we can connect to the mysql db 
    # TODO: are we loading the mysql info from the plugin.yaml file from RB?
    dbPrefix = ""
    try:
        mysqlDb = myconnpy_database.Database()
        connectDict = myconnpy_database.Config.dbinfo()
        # now modify this dictionary with the info the yaml config file
        connectDict["database"] = args.rbYamlConfig["realistic_biomes"]["database_name"]
        connectDict["host"] = args.rbYamlConfig["realistic_biomes"]["database_host"]
        connectDict["port"] = args.rbYamlConfig["realistic_biomes"]["database_port"]
        connectDict["user"] = args.rbYamlConfig["realistic_biomes"]["database_user"]
        connectDict["password"] = args.rbYamlConfig["realistic_biomes"]["database_password"]
        dbPrefix = args.rbYamlConfig["realistic_biomes"]["database_prefix"]

        mysqlDb.connect(connectDict)
    except Exception as e:
        print("Something went wrong when connecting to the mysql database, error was {}".format(e))
        printTraceback()
        sys.exit(1)


    # now connected to both databases fine
    sqliteCur = sqliteDb.cursor()
    mysqlCur = mysqlDb.cursor()


    # try and see if the old databases are there, if so, delete them!
    # if the table does exist, then we get something like: [('rb_chunk',)]
    # if not, we get an empty list, so we can just do bool(result) to see if 
    # the table exists
    # 
    # we need to do this, since we name our foreign key constraints when we create the table
    # and if the table exists, then it throws an error saying it cant create the table because
    # the names of the foreign key constraints are taken!

    mysqlCur.execute('''SHOW TABLES LIKE '{}_plant' '''.format(dbPrefix))
    tmpResult2 = mysqlCur.fetchall()

    if tmpResult2:

        queryResult2 = query_yes_no("The table '{}_plant' already exists! We are going to execute DROP TABLE on this, are you sure?".format(dbPrefix))

        if not queryResult2:
            sys.exit("You answered no, but we need to drop the already existing {}_plant table before continuing! exiting.".format(dbPrefix))
        else:
            # user answered yes, drop the table
            mysqlCur.execute("DROP TABLE {}_plant".format(dbPrefix))


    # check for plant table
    mysqlCur.execute('''SHOW TABLES LIKE '{}_chunk' '''.format(dbPrefix))
    tmpResult = mysqlCur.fetchall()
    if tmpResult:

        queryResult = query_yes_no("The table '{}_chunk' already exists! We are going to execute DROP TABLE on this, are you sure?".format(dbPrefix))

        if not queryResult:
            sys.exit("You answered no, but we need to drop the already existing {}_chunk table before continuing! exiting.".format(dbPrefix))

        else:
            # user answered yes, drop the table
            mysqlCur.execute("DROP TABLE {}_chunk".format(dbPrefix))





    # create the tables in the mysql database
    mysqlCur.execute('''CREATE TABLE IF NOT EXISTS {}_chunk (id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         w INTEGER, x INTEGER, z INTEGER, 
                         INDEX chunk_coords_idx (w, x, z)) ENGINE INNODB;'''.format(dbPrefix))

    mysqlCur.execute('''CREATE TABLE IF NOT EXISTS {}_plant
                        (chunkId BIGINT, w INTEGER, x INTEGER, y INTEGER, z INTEGER, date INTEGER UNSIGNED, growth REAL, 
                        INDEX plant_chunk_idx (chunkId), 
                        CONSTRAINT chunkIdConstraint FOREIGN KEY (chunkId) REFERENCES {}_chunk (id))
                        ENGINE INNODB;'''.format(dbPrefix,dbPrefix))


    # now, get entries for the two tables from sqlite, and then put that into the insert statement for mysql

    # **************
    # table one
    # **************

    countResult = sqliteCur.execute("select count(*) from chunk")
    chunkTotal = sqliteCur.fetchone()[0]
    sqliteCur.execute("select * from chunk")

    result = sqliteCur.fetchall()

    # here we get back something like:
    # [(1, 0, 569, 557)]
    # so what we do is we have a dictionary, that maps a number to the value so we can do prepared statements 
    # and whatnot
    chunkDict = {}
    counter = 0
    chunkInsertStr = "INSERT INTO {}_chunk (id, w, x, z) VALUES (%(1)s, %(2)s, %(3)s, %(4)s)".format(dbPrefix)

    for iterResult in result:

        if counter % 1000 == 0:
            print("inserting {} / {} into {}_chunk".format(counter, chunkTotal, dbPrefix))

        # execute query
        chunkDict = {"1": iterResult[0], "2": iterResult[1], "3": iterResult[2], "4":iterResult[3]}
        mysqlCur.execute(chunkInsertStr, chunkDict)

        counter += 1

    print("FINISHED inserting {} / {} into {}_chunk".format(counter, chunkTotal, dbPrefix))


    # **************
    # table two
    # **************

    countResult = sqliteCur.execute("select count(*) from plant")
    plantTotal = sqliteCur.fetchone()[0]
    sqliteCur.execute("select * from plant")

    result = sqliteCur.fetchall()

    # here we get back something like:
    # [(1, 0, 569, 557)]
    # so what we do is we have a dictionary, that maps a number to the value so we can do prepared statements 
    # and whatnot
    plantDict = {}
    counter = 0
    plantInsertStr = "INSERT INTO {}_plant (chunkId, w, x, y, z, date, growth) VALUES (%(1)s, %(2)s, %(3)s, %(4)s, %(5)s, %(6)s, %(7)s)".format(dbPrefix)
    
    for iterResult in result:

        if counter % 1000 == 0:
            print("inserting {} / {} into {}_plant".format(counter, plantTotal, dbPrefix))

        # execute query
        plantDict = {"1": iterResult[0], "2": iterResult[1], "3": iterResult[2], "4":iterResult[3], "5": iterResult[4], 
            "6": iterResult[5] / 1000, "7":iterResult[6] } # make sure to divide by 1000, (for iterResult[5]) so we go from milliseconds to seconds
        mysqlCur.execute(plantInsertStr, plantDict)

        counter += 1

    print("FINISHED inserting {} / {} into {}_plant".format(counter, plantTotal, dbPrefix))



    # close db stuff
    mysqlDb.commit()

    mysqlCur.close()
    sqliteCur.close()
    mysqlDb.close()
    sqliteDb.close()


def query_yes_no(question, default="yes"):
    """Ask a yes/no question via input() and return their answer.

    "question" is a string that is presented to the user.
    "default" is the presumed answer if the user just hits <Enter>.
        It must be "yes" (the default), "no" or None (meaning
        an answer is required of the user).

    The "answer" return value is one of "yes" or "no".
    """
    valid = {"yes":True,   "y":True,  "ye":True,
             "no":False,     "n":False}
    if default == None:
        prompt = " [y/n] "
    elif default == "yes":
        prompt = " [Y/n] "
    elif default == "no":
        prompt = " [y/N] "
    else:
        raise ValueError("invalid default answer: '%s'" % default)

    while True:
        sys.stdout.write(question + prompt)
        choice = input().lower()
        if default is not None and choice == '':
            return valid[default]
        elif choice in valid:
            return valid[choice]
        else:
            sys.stdout.write("Please respond with 'yes' or 'no' "\
                             "(or 'y' or 'n').\n")

def isYamlType(stringArg):
    ''' helper method for argparse that sees if the argument is a valid yaml file
    @param stringArg - the argument we get from argparse
    @return the object returned from safe_load,or exception '''

    test = None
    try:
        with open(stringArg, encoding="utf-8") as f:
            test=yaml.safe_load(f)
        # if it succeeds, then return the object
        return test
    except yaml.YAMLError as e:
        if hasattr(e, "problem_mark"):
            mark=e.problem_mark
            raise argparse.ArgumentTypeError("Problem parsing yaml: error was at {}:{}".format(mark.line+1, mark.column+1))
    except OSError as e2:
        raise argparse.ArgumentTypeError("error reading file at {}, error was {}".format(stringArg, e2))            
    
def isSqliteType(dbLocationArg):
        ''' see if the file we wanted to connect to actually is a database,
        raises an ArgumentTypeError if its not a sqlite3 database
        @param dbLocationArg - the location of the database from parser.parse_args()
        @return the sqlite3 db connection, else we raise an exception
        '''

        dbLocation = os.path.realpath(dbLocationArg)

        # make sure this is a file
        if not os.path.isfile(dbLocation):
            raise argparse.ArgumentTypeError("{} is not a file!".format(dbLocation))

        # make the connection
        tmpConnection = sqlite3.connect(dbLocation)
        tmpCursor = tmpConnection.cursor()
            
        try:
            # try getting something from the master table
            tmpCursor.execute('''select * from sqlite_master limit 1''')
        except sqlite3.DatabaseError as e:
            tmpCursor.close()
            tmpConnection.close()
            raise argparse.ArgumentTypeError("the file at {} is not a sqlite3 database! error: {}".format(dbLocation, e))

        except Exception as e1:
            tmpCursor.close()
            tmpConnection.close()
            raise argparse.ArgumentTypeError("Something else went wrong with the sqlite db at {}, error: {}".format(dbLocation, e))
        
        # table is an actual sqlite3 table
        return tmpConnection

def printTraceback():
    '''prints the traceback'''

    # get variables for the method we are about to call
    exc_type, exc_value, exc_traceback = sys.exc_info()

    # print exception
    traceback.print_exception(exc_type, exc_value, exc_traceback)

if __name__ == "__main__":
    # if we are being run as a real program

    parser = argparse.ArgumentParser(description="Converts a sqlite3 database to a mysql one for realistic biomes", 
    epilog="Copyright Mark Grandi, June 11, 2013")

    parser.add_argument("sqliteDb", type=isSqliteType, help="the input sqlite database")
    parser.add_argument("rbYamlConfig", type=isYamlType, help="the realistic biomes yaml configuration file")

    try:
        convertSqliteToMysql(parser.parse_args())
    except Exception as e: 
        print("Something went wrong...error: {}".format(e))
        print("##################")
        printTraceback()
        print("##################")
        sys.exit(1)