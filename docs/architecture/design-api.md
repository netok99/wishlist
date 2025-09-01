# Design da API - Wishlist Service
## Visão Geral da API
O Wishlist Service expõe uma API REST seguindo princípios RESTful e padrões modernos de design de API.

## Princípios de Design
- RESTful: Recursos bem definidos com verbos HTTP apropriados.
- Consistente: Padrões consistentes em toda a API.
- Autodocumentada: URLs e responses intuitivas.
- Versionada: Suporte a evolução da API.
- Segura: Validação e rate limiting.

## Estrutura da API
URL Base:
```
https://api.ecommerce.com/wishlist/v1
```

## Hierarquia de Recursos
```
/customers/{customerId}/wishlist/
├── GET    /                           # Buscar wishlist completa
├── DELETE /                           # Limpar wishlist
└── /products/{productId}
    ├── POST   /                       # Adicionar produto
    ├── GET    /                       # Verificar se produto existe
    └── DELETE /                       # Remover produto
```

## Detalhes dos Endpoints:
Após rodar a aplicação acesse a url: 
```
http://localhost:8082/swagger-ui/index.html
```
