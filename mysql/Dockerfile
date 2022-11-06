FROM mysql
ADD init/schema-mysql.sql /docker-entrypoint-initdb.d/

EXPOSE 3306
CMD ["mysqld"]