## todo deployment

### docker compose

```shell
# Be in deployment directory
docker compose up
```

### docker swarm

```shell
docker stack deploy --compose-file docker-todo-stack.yml  --with-registry-auth --detach=false todo
```