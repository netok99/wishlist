# Design do Banco de Dados - Wishlist Service
## Visão Geral do Banco
O Wishlist Service utiliza MongoDB como banco de dados principal, aproveitando sua capacidade de armazenar documentos que se alinham naturalmente com o domínio da wishlist.

## Modelo de Dados
### Documento Wishlist
```javascript{
{
  "_id": ObjectId("64a7b8c9d1e2f3456789"),
  "customerId": "customer123",
  "products": [
    {
      "productId": "product456",
      "addedAt": ISODate("2024-08-29T10:30:00Z")
    },
    {
      "productId": "product789", 
      "addedAt": ISODate("2024-08-29T11:45:00Z")
    }
  ],
  "createdAt": ISODate("2024-08-29T09:00:00Z"),
  "updatedAt": ISODate("2024-08-29T11:45:00Z")
}
```

## Validação de Schema
```javascript
db.createCollection("wishlists", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["customerId", "products", "createdAt", "updatedAt"],
      properties: {
        customerId: {
          bsonType: "string",
          description: "ID do cliente é obrigatório e deve ser string"
        },
        products: {
          bsonType: "array",
          maxItems: 20,
          items: {
            bsonType: "object",
            required: ["productId", "addedAt"],
            properties: {
              productId: { bsonType: "string" },
              addedAt: { bsonType: "date" }
            }
          }
        },
        createdAt: { bsonType: "date" },
        updatedAt: { bsonType: "date" }
      }
    }
  }
});
```
## Decisões de Design
### Documento por Cliente
✅ Vantagem: Operações atômicas em toda wishlist

✅ Vantagem: Agregação natural dos dados do cliente

✅ Vantagem: Eficiente para padrões típicos de acesso

⚠️ Desvantagem: Tamanho do documento cresce com produtos (limitado a 20)


### Array de Produtos Embutido
✅ Vantagem: Todos produtos recuperados em uma query

✅ Vantagem: Atualizações atômicas com $addToSet e $pull

✅ Vantagem: Não necessita operações JOIN

⚠️ Desvantagem: Não pode consultar produtos entre clientes facilmente

## Índices
### Índices Principais
```javascript
// Índice único no customerId para buscas rápidas
db.wishlists.createIndex({ "customerId": 1 }, { unique: true });

// Índice nos IDs dos produtos para consultas baseadas em produtos
db.wishlists.createIndex({ "products.productId": 1 });

// Índice composto para cliente + produto
db.wishlists.createIndex({ 
  "customerId": 1, 
  "products.productId": 1 
});
```

### Índices Secundários
```javascript
// Índices temporais para analytics
db.wishlists.createIndex({ "createdAt": 1 });
db.wishlists.createIndex({ "updatedAt": 1 });
```

## Padrões de Query
### Operações Comuns
### Buscar Wishlist do Cliente
```javascript
db.wishlists.updateOne(
  { 
    "customerId": "customer123",
    "products": { $not: { $elemMatch: { "productId": "product456" } } },
    $expr: { $lt: [{ $size: "$products" }, 20] }
  },
  { 
    $addToSet: { 
      "products": { 
        "productId": "product456", 
        "addedAt": new Date() 
      }
    },
    $set: { "updatedAt": new Date() }
  },
  { upsert: true }
);
```

### Remover Produto
```javascript
db.wishlists.updateOne(
  { "customerId": "customer123" },
  { 
    $pull: { "products": { "productId": "product456" } },
    $set: { "updatedAt": new Date() }
  }
);
```

## Características de Performance
### Performance de Leitura
- Buscar Wishlist: O(1) com índice customerId
- Verificar Produto: O(log n) com índice composto
- Resposta Típica: < 5ms para queries indexadas

### Performance de Escrita
- Adicionar Produto: O(1) operação atômica
- Remover Produto: O(n) onde n = produtos na wishlist (máx 20)
- Resposta Típica: < 10ms para operações de escrita

## Considerações Operacionais
### Estratégia de Backup
```bash
# Backups diários com recuperação point-in-time
mongodump --uri="mongodb://..." --gzip --out=/backup/$(date +%Y%m%d)
```

### Scripts de Migração
```javascript
// Exemplo: Adicionar novo campo a documentos existentes
db.wishlists.updateMany(
  { "version": { $exists: false } },
  { $set: { "version": 1 } }
);
```
