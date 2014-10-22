import sys
try:
    import mysql.connector
except ImportError as e:
    print("you need myconnpy! Get it from: https://launchpad.net/myconnpy , error: {}".format(e))
    sys.exit(1)

class Config:
    '''simple config class for the database'''

    # configuration options
    HOST = "__name__"
    DATABASE = "__database__"
    USER = "__user__"
    PASSWORD = "__password__"
    PORT = 3306
    CHARSET = "utf8"
    UNICODE = True
    WARNINGS = True

    @classmethod
    def dbinfo(cls):
        ''' class method that returns a dictionary of 
        the myconnpy configuration options.'''

        return {
            'host': cls.HOST,
            'port': cls.PORT,
            'database': cls.DATABASE,
            'user': cls.USER,
            'password': cls.PASSWORD,
            'charset': cls.CHARSET,
            'use_unicode': cls.UNICODE,
            'get_warnings': cls.WARNINGS,
            }       

class Database:
    ''' class that we use to access the database'''


    _connection = None

    def __init__(self):
        ''' constructor'''

        pass


    def connect(self, configDict):
        ''' connects to the database
        @param configDict - the dictionary returned by the Config.dbinfo() method'''

        try:
            self._connection = mysql.connector.connect(**configDict)
        except Exception as e:
            self.close()
            raise Exception("Unable to connect to the database!") from e

    def cursor(self):
        ''' returns a cursor object from the db
        @return a cursor object'''

        if self._connection is not None:

            return self._connection.cursor()

        else:

            raise Exception("We arn't connected to the database yet!")

    def commit(self):
        ''' calls commit on the _connection'''


        if self._connection is not None:

            return self._connection.commit()

        else:

            raise Exception("We arn't connected to the database yet!")

    
    def close(self):
        ''' close the connection'''
        if self._connection:

            self._connection.close()

        self._connection = None
