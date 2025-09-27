# Use a specific version of OpenJDK
FROM ghcr.io/graalvm/jdk-community:25

# Set the working directory
WORKDIR /app

# # Copy only the necessary source files
# COPY HelloWorld.java .
#
# # Compile the Java program
# RUN javac HelloWorld.java

COPY ansible/build/civ-apps/queue-1.0.0-all.jar queue.jar

# Set the default command to run the Java program
CMD ["java", "-Xms1G","-Xmx1G","-XX:+UseG1GC","-XX:G1HeapRegionSize=4M", "-XX:+UnlockExperimentalVMOptions", "-XX:+ParallelRefProcEnabled", "-XX:+AlwaysPreTouch", "-Dlog4j2.formatMsgNoLookups=true", "-XX:+UseStringDeduplication", "-Dfile.encoding=UTF-8", "-Duser.timezone=UTC", "-Dapp.dir=/data", "-jar", "queue.jar"]
#CMD ["sh"]
