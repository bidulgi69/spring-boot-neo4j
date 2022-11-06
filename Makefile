clean:
	docker-compose down --remove-orphans && \
	docker rmi $$(docker images | grep -e none -e spring-neo4j_mysql) && \
	docker volume rm $$(docker volume ls -qf dangling=true)
