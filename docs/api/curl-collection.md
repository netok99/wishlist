# Curl Collection

### Recuperar Wishlist
```bash
curl --request GET \
  --url http://localhost:8082/api/v1/customers/1/wishlist
```

### Adicionar produto a lista de desejos
```bash
curl --request POST \
  --url http://localhost:8082/api/v1/customers/1/wishlist/products/1
```

### Checa existência do produto na lista de desejos 
```bash
curl --request GET \
  --url http://localhost:8082/api/v1/customers/1/wishlist/products/1
```

### Remove produto a lista de desejos 
```bash
curl --request DELETE \
  --url http://localhost:8082/api/v1/customers/1/wishlist/products/1
```

### (Extra) Remove todos os produtos da lista de desejos 
```bash
curl --request DELETE \
  --url http://localhost:8082/api/v1/customers/1/wishlist
```
