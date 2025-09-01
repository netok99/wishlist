# Wishlist Service

## Descrição 
Microserviço para gerenciamento de listas de desejos em plataforma de e-commerce.

## Quick Start
```bash
# Clone do repositório
git clone <repository-url>
cd wishlist

# Executar com Docker
docker-compose up -d

# Executar localmente
./gradlew bootRun
```

## Funcionalidades
- ✅ Adicionar produtos à wishlist (máximo 20 produtos)
- ✅ Remover produtos da wishlist
- ✅ Consultar wishlist completa do cliente
- ✅ Verificar existência de produto específico
- ✅ Limpar wishlist completamente
- ✅ Validações de negócio (duplicatas, limites)

## Stack Tecnológica
- Java 21 
- Spring Boot 3.1.5
- MongoDB
- Gradle
- Docker
- TestContainers

## Documentação
Acesse a pasta docs na raiz do projeto para mais detalhes.

## 🚀 Deploy e Operação
- Docker Setup: Ambiente local completo
- Configuração Gradle: Build com Kotlin DSL
- Configurações: Profiles e configs

## Testes
- Unitários: Cobertura > 90% com JUnit 5 + AssertJ
- BDD: Cucumber para testes comportamentais
- Integração: TestContainers com MongoDB real
- API: MockMvc para testes de endpoints

## Executando o Projeto
Pré-requisitos: Java 17+, Docker e Docker Compose

### Ambiente
```bash
# Subir tudo com Docker
docker-compose up -d

# Verificar logs
docker-compose logs -f wishlist-api
```

### Testes
```bash
# Todos os testes
./gradlew test

# Apenas testes unitários
./gradlew test --tests "*Test" --exclude-task integrationTest

# Testes de integração
./gradlew integrationTest

# Testes BDD (Cucumber)
./gradlew cucumberTest

# Relatório de cobertura
./gradlew test jacocoTestReport
```

### Padrões de Código
- Seguir convenções Clean Code
- Testes obrigatórios para novas features
- Documentar ADRs para decisões arquiteturais importantes
- Usar padrão BDD para testes comportamentais


**Última atualização: 01-09-2024**
