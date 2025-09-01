# ADR-001: Seleção do MongoDB como Banco de Dados para o Serviço de Wishlist

## Descrição
Este documento registra a decisão arquitetural sobre a escolha do MongoDB como sistema de gerenciamento de banco de dados para o microserviço responsável por gerenciar as listas de desejos dos clientes na plataforma de e-commerce.

## Contexto
O microserviço de Wishlist precisa de uma solução de persistência que atenda aos seguintes requisitos funcionais e não-funcionais:

### Requisitos funcionais
- Armazenar wishlists de clientes com até 20 produtos por lista
- Operações de adição, remoção e consulta de produtos na wishlist
- Verificação de existência de produtos específicos
- Consulta completa da wishlist do cliente

### Requisitos não funcionais
- **Performance**: Operações de leitura e escrita com baixa latência
- **Escalabilidade**: Capacidade de crescer horizontalmente conforme demanda
- **Disponibilidade**: Alta disponibilidade para operações críticas do e-commerce
- **Consistência**: Garantia de integridade dos dados da wishlist
- **Simplicidade**: Facilidade de desenvolvimento e manutenção

### Características do domínio
- Estrutura de dados simples: um documento por cliente
- Relacionamentos desnormalizados (produtos como array dentro da wishlist)
- Queries predominantemente por chave primária (customerId)
- Volume de escritas moderado, com picos durante promoções
- Padrão de acesso: mais leituras que escritas

### Contexto Técnico
- Arquitetura de microserviços
- Stack Java com Spring Boot
- Deployment em containers (Docker/Kubernetes)
- Equipe com experiência em bancos NoSQL

## Decisão

**Escolhemos o MongoDB como banco de dados principal para o serviço de Wishlist.**

A implementação utilizará:
- **MongoDB Community/Atlas** como motor de banco de dados
- **Spring Data MongoDB** para abstração de acesso aos dados
- **MongoTemplate** para queries complexas e operações atômicas

## Razão

### Alinhamento Perfeito com o Modelo de Dados

O MongoDB oferece um **mapeamento natural** para o domínio da wishlist:

```json
{
  "_id": "objectId",
  "customerId": "cliente123",
  "products": [
    {
      "productId": "produto456",
      "addedAt": "2024-08-29T10:30:00Z"
    }
  ],
  "createdAt": "2024-08-29T09:00:00Z",
  "updatedAt": "2024-08-29T10:30:00Z"
}
```

### Vantagens Técnicas Decisivas

1. **Operações Atômicas Nativas**
   - `$addToSet` para adicionar produtos sem duplicatas
   - `$pull` para remover produtos específicos
   - `findAndModify` para operações complexas em uma única chamada

2. **Performance Otimizada**
   - Uma wishlist = um documento: operações O(1)
   - Índices específicos por `customerId` e `products.productId`
   - Queries simples sem JOINs complexos

3. **Validação de Schema Nativa**
   - Validação do limite de 20 produtos no nível do banco
   - Estrutura de dados garantida por JSON Schema

4. **Escalabilidade Horizontal**
   - Sharding nativo baseado em `customerId`
   - Replica sets para alta disponibilidade
   - Auto-scaling no MongoDB Atlas

### Integração com o Ecossistema
- **Spring Data MongoDB**: Integração madura e bem documentada
- **Spring Boot**: Configuração automática e starter dedicado
- **MongoTemplate**: Flexibilidade para queries avançadas
- **Testcontainers**: Testes de integração com MongoDB real

### Facilidade Operacional
- **MongoDB Atlas**: Managed service com backup automático
- **Monitoring**: Ferramentas nativas de observabilidade
- **Docker**: Imagens oficiais para desenvolvimento local
- **Community**: Ampla base de conhecimento e suporte

## Alternativas

### Redis (Considerada)
**Vantagens:**
- Performance excepcional (operações em memória)
- Estruturas de dados nativas (Sets, Hashes)
- TTL automático para limpeza de dados

**Desvantagens:**
- **Persistência**: Requer configuração cuidadosa para durabilidade
- **Memoria**: Custo elevado para grandes volumes de dados
- **Queries complexas**: Limitado para operações avançadas
- **Backup**: Estratégias mais complexas

**Motivo da Rejeição:** Custo elevado de memória e complexidade operacional para persistência durável.

### DynamoDB (Considerada)

**Vantagens:**
- Serverless e auto-escalável
- Performance consistente e previsível
- Managed service completo

**Desvantagens:**
- **Vendor Lock-in**: Específico da AWS
- **Custo**: Pode ser elevado com crescimento dos dados
- **Flexibilidade**: Limitações em queries e índices secundários
- **Curva de aprendizado**: Paradigmas específicos do DynamoDB

**Motivo da Rejeição:** Dependência de cloud provider específico e limitações de flexibilidade.

### PostgreSQL com JSONB (Considerada)

**Vantagens:**
- Transações ACID completas
- Queries SQL familiares
- Suporte nativo a JSON
- Maturidade e estabilidade

**Desvantagens:**
- **Complexidade**: Over-engineering para o caso de uso
- **Performance**: JOINs desnecessários para operações simples
- **Schema**: Rigidez estrutural inadequada para documentos
- **Escalabilidade**: Sharding mais complexo

**Motivo da Rejeição:** Complexidade desnecessária e mismatch com o modelo de dados orientado a documentos.

## Consequências

### Positivas

1. **Desenvolvimento Acelerado**
   - Mapeamento direto entre objetos Java e documentos MongoDB
   - Menos código boilerplate para serialização/deserialização
   - Queries intuitivas e expressivas

2. **Performance Superior**
   - Operações de wishlist em O(1) ou O(log n)
   - Cache de documentos frequentes no nível do banco
   - Índices otimizados para padrões de acesso específicos

3. **Operações Simplificadas**
   - Backup e restore nativos
   - Monitoring integrado
   - Escalabilidade transparente

4. **Flexibilidade Futura**
   - Adição de campos sem migração de schema
   - Evolução incremental da estrutura de dados
   - Suporte a features avançadas (geolocalização, text search)

### Negativas e Mitigações

1. **Consistência Eventual**
   - **Impacto**: Replica sets podem ter lag de replicação
   - **Mitigação**: Read preference configurado para primary, write concern majority

2. **Consumo de Armazenamento**
   - **Impacto**: BSON pode ser menos eficiente que formatos binários
   - **Mitigação**: Compressão nativa (WiredTiger) e TTL para dados antigos

3. **Curva de Aprendizado**
   - **Impacto**: Paradigmas NoSQL podem ser novos para parte da equipe
   - **Mitigação**: Treinamento focado e documentação interna detalhada

4. **Vendor Lock-in Potencial**
   - **Impacto**: Dependência de features específicas do MongoDB
   - **Mitigação**: Abstração via repository pattern e interfaces bem definidas

### Métricas de Sucesso

Para validar esta decisão, acompanharemos:

- **Latência**: P95 < 100ms para operações de CRUD
- **Throughput**: Suporte a 1000+ operações/segundo por instância
- **Disponibilidade**: 99.9% uptime mensal
- **Desenvolvimento**: Redução de 30% no tempo de implementação vs SQL
- **Escalabilidade**: Auto-scaling baseado em CPU/memória < 80%

### Ponto de Revisão

Esta decisão será revisada em **6 meses** ou quando:
- Volume de dados exceder 100GB por shard
- Latência média superar 50ms
- Surgir necessidade crítica de transações multi-documento
- Mudanças significativas nos padrões de acesso aos dados

---

**Status:** Aprovado
**Data:** 31/08/2024
**Revisor Técnico:** Arquiteto de Sistemas
**Última Atualização:** 31/08/2024
