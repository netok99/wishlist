# Estratégia de Testes - Wishlist Service

## Visão Geral da Estratégia
O Wishlist Service implementa uma estratégia abrangente de testes baseada na pirâmide de testes, com foco em BDD (Behavior-Driven Development) e Clean Architecture.

##️ Pirâmide de Testes
```
      🔺 E2E Tests
    🔸🔸🔸 API Tests  
  🔹🔹🔹🔹🔹 Integration Tests
🔷🔷🔷🔷🔷🔷🔷 Unit Tests
```

## Tipos de Teste por Camada
### Testes Unitários (Base da Pirâmide)
Escopo: Testam classes individuais isoladamente

Ferramentas: JUnit 5, Mockito, AssertJ

Foco: Regras de negócio, lógica de domínio

Velocidade: Muito rápido (< 1s para toda suíte)

### Testes de Integração
Escopo: Testam integração entre componentes

Ferramentas: TestContainers, @DataMongoTest

Foco: Repository, MongoDB, configurações

Velocidade: Rápido (< 30s para suíte)

### Testes de API
Escopo: Testam endpoints HTTP end-to-end

Ferramentas: MockMvc, TestRestTemplate

Foco: Controllers, serialização, validação

Velocidade: Médio (< 2min para suíte)

### Testes E2E
Escopo: Testam fluxo completo do usuário

Ferramentas: Cucumber, TestContainers

Foco: Cenários de negócio, jornada do usuário

Velocidade: Lento (< 10min para suíte)

## Estrutura de Testes BDD
```java
@DisplayName("Given customer wants to add product to wishlist")
class GivenCustomerWantsToAddProduct {
    
    @Test
    @DisplayName("When wishlist is empty, Then should add product successfully")
    void whenWishlistIsEmpty_thenShouldAddProductSuccessfully() {
        // Given - Estado inicial
        Wishlist emptyWishlist = new Wishlist("customer123");
        
        // When - Ação executada
        emptyWishlist.addProduct("product456");
        
        // Then - Resultado esperado
        assertThat(emptyWishlist.hasProduct("product456")).isTrue();
        assertThat(emptyWishlist.getProductCount()).isEqualTo(1);
    }
}
```

## Testes por Camada da Clean Architecture
### Testes de Domínio (Entidades)
```java
@DisplayName("Wishlist Domain Entity")
class WishlistBehaviorTest {
    
    @Test
    @DisplayName("Should not allow more than 20 products")
    void shouldNotAllowMoreThan20Products() {
        // Given - Wishlist com 20 produtos
        Wishlist fullWishlist = createWishlistWithProducts(20);
        
        // When/Then - Tentar adicionar 21º produto
        assertThatThrownBy(() -> fullWishlist.addProduct("product21"))
            .isInstanceOf(IllegalStateException.class)
```
