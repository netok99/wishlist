package com.wishlist.acceptance.steps;

import com.wishlist.application.dto.AddProductResponse;
import com.wishlist.application.dto.ProductExistsResponse;
import com.wishlist.application.dto.WishlistResponse;
import com.wishlist.application.exception.InvalidCustomerIdException;
import com.wishlist.application.exception.InvalidProductIdException;
import com.wishlist.application.exception.ProductAlreadyExistsException;
import com.wishlist.application.exception.ProductNotFoundException;
import com.wishlist.application.exception.WishlistLimitExceededException;
import com.wishlist.application.service.WishlistService;
import com.wishlist.domain.entity.Wishlist;
import com.wishlist.domain.repository.WishlistRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@CucumberContextConfiguration
@SpringBootTest
@TestPropertySource(properties = {
    "spring.mongodb.embedded.version=4.4.0",
    "logging.level.com.ecommerce.wishlist=DEBUG"
})
public class WishlistStepDefinitions {
    @Autowired
    private WishlistService wishlistService;
    @Autowired
    private WishlistRepository wishlistRepository;
    private String currentCustomerId;
    private String currentProductId;
    private WishlistResponse currentWishlistResponse;
    private AddProductResponse currentAddResponse;
    private ProductExistsResponse currentExistsResponse;
    private Exception currentException;

    @Given("the wishlist system is working")
    public void the_wishlist_system_is_working() {
        assertThat(wishlistService).isNotNull();
        assertThat(wishlistRepository).isNotNull();
    }

    @Given("there is a customer with ID {string}")
    public void there_is_a_customer_with_id(String customerId) {
        this.currentCustomerId = customerId;
    }

    @Given("the customer {string} has an empty wishlist")
    public void the_customer_has_an_empty_wishlist(String customerId) {
        this.currentCustomerId = customerId;
        if (wishlistRepository.existsByCustomerId(customerId)) {
            wishlistRepository.deleteByCustomerId(customerId);
        }
    }

    @Given("the customer {string} wishlist contains product {string}")
    public void the_customer_wishlist_contains_product(
        String customerId,
        String productId
    ) {
        this.currentCustomerId = customerId;
        this.currentProductId = productId;

        final Wishlist wishlist = wishlistRepository
            .findByCustomerId(customerId)
            .orElse(new Wishlist(customerId));

        if (!wishlist.hasProduct(productId)) {
            wishlist.addProduct(productId);
            wishlistRepository.save(wishlist);
        }
    }

    @Given("the customer {string} wishlist contains the products:")
    public void the_customer_wishlist_contains_the_products(
        String customerId,
        DataTable dataTable
    ) {
        this.currentCustomerId = customerId;

        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);

        Wishlist wishlist = new Wishlist(customerId);

        for (Map<String, String> product : products) {
            String productId = product.get("productId");
            wishlist.addProduct(productId);
        }

        wishlistRepository.save(wishlist);
    }

    @Given("the customer {string} already has {int} products in wishlist")
    public void the_customer_already_has_products_in_wishlist(String customerId, int quantity) {
        this.currentCustomerId = customerId;

        Wishlist wishlist = new Wishlist(customerId);

        for (int i = 1; i <= quantity; i++) {
            wishlist.addProduct("product" + String.format("%03d", i));
        }

        wishlistRepository.save(wishlist);
    }

    @Given("the customer {string} has {int} products in wishlist")
    public void the_customer_has_products_in_wishlist(String customerId, int quantity) {
        the_customer_already_has_products_in_wishlist(customerId, quantity);
    }

    @When("I add product {string} to the wishlist")
    public void i_add_product_to_the_wishlist(String productId) {
        this.currentProductId = productId;
        try {
            this.currentAddResponse = wishlistService.addProduct(currentCustomerId, productId);
            this.currentException = null;
        } catch (Exception e) {
            this.currentException = e;
        }
    }

    @When("I add the following products to the wishlist:")
    public void i_add_the_following_products_to_the_wishlist(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> product : products) {
            String productId = product.get("productId");
            try {
                wishlistService.addProduct(currentCustomerId, productId);
            } catch (Exception e) {
                this.currentException = e;
                break;
            }
        }
    }

    @When("I try to add product {string} again")
    public void i_try_to_add_product_again(String productId) {
        try {
            this.currentAddResponse = wishlistService.addProduct(currentCustomerId, productId);
            this.currentException = null;
        } catch (Exception e) {
            this.currentException = e;
        }
    }

    @When("I try to add product {string}")
    public void i_try_to_add_product(String productId) {
        i_try_to_add_product_again(productId);
    }

    @When("I remove product {string}")
    public void i_remove_product(String productId) {
        this.currentProductId = productId;
        try {
            wishlistService.removeProduct(currentCustomerId, productId);
            this.currentException = null;
        } catch (Exception e) {
            this.currentException = e;
        }
    }

    @When("I try to remove product {string}")
    public void i_try_to_remove_product(String productId) {
        i_remove_product(productId);
    }

    @When("I query the complete wishlist")
    public void i_query_the_complete_wishlist() {
        try {
            this.currentWishlistResponse = wishlistService.getWishlist(currentCustomerId);
            this.currentException = null;
        } catch (Exception e) {
            this.currentException = e;
        }
    }

    @When("I check if product {string} exists in the wishlist")
    public void i_check_if_product_exists_in_the_wishlist(String productId) {
        this.currentProductId = productId;
        try {
            this.currentExistsResponse = wishlistService.checkProductExists(currentCustomerId, productId);
            this.currentException = null;
        } catch (Exception e) {
            this.currentException = e;
        }
    }

    @When("I clear the wishlist completely")
    public void i_clear_the_wishlist_completely() {
        try {
            wishlistService.clearWishlist(currentCustomerId);
            this.currentException = null;
        } catch (Exception e) {
            this.currentException = e;
        }
    }

    @When("I try to {string} with customerId {string} and productId {string}")
    public void i_try_to_operation_with_ids(String action, String customerId, String productId) {
        try {
            switch (action) {
                case "add product":
                    wishlistService.addProduct(customerId, productId);
                    break;
                case "remove product":
                    wishlistService.removeProduct(customerId, productId);
                    break;
                case "query wishlist":
                    wishlistService.getWishlist(customerId);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized action: " + action);
            }
            this.currentException = null;
        } catch (Exception e) {
            this.currentException = e;
        }
    }

    @Then("the product should be added successfully")
    public void the_product_should_be_added_successfully() {
        assertThat(currentException).isNull();
        assertThat(currentAddResponse).isNotNull();
        assertThat(currentAddResponse.productId()).isEqualTo(currentProductId);
        assertThat(currentAddResponse.customerId()).isEqualTo(currentCustomerId);
        assertThat(currentAddResponse.message()).contains("successfully");
    }

    @Then("the wishlist should contain {int} product(s)")
    public void the_wishlist_should_contain_products(int expectedQuantity) {
        WishlistResponse response = wishlistService.getWishlist(currentCustomerId);
        assertThat(response.totalItems()).isEqualTo(expectedQuantity);
    }

    @Then("product {string} should be present in the wishlist")
    public void product_should_be_present_in_the_wishlist(String productId) {
        try {
            ProductExistsResponse response = wishlistService.checkProductExists(currentCustomerId, productId);
            assertThat(response.exists()).isTrue();
        } catch (ProductNotFoundException e) {
            fail("Product should be present in wishlist");
        }
    }

    @Then("all products should be present in the wishlist")
    public void all_products_should_be_present_in_the_wishlist() {
        WishlistResponse response = wishlistService.getWishlist(currentCustomerId);
        assertThat(response.products()).isNotEmpty();
        assertThat(response.totalItems()).isGreaterThan(0);
    }

    @Then("it should return {string} error")
    public void it_should_return_error(String errorType) {
        assertThat(currentException).isNotNull();

        switch (errorType) {
            case "product already exists":
                assertThat(currentException).isInstanceOf(ProductAlreadyExistsException.class);
                break;
            case "wishlist limit exceeded":
                assertThat(currentException).isInstanceOf(WishlistLimitExceededException.class);
                break;
            case "product not found":
                assertThat(currentException).isInstanceOf(ProductNotFoundException.class);
                break;
            case "invalid customer ID":
                assertThat(currentException).isInstanceOf(InvalidCustomerIdException.class);
                break;
            case "invalid product ID":
                assertThat(currentException).isInstanceOf(InvalidProductIdException.class);
                break;
            default:
                fail("Unrecognized error type: " + errorType);
        }
    }

    @Then("the wishlist should continue with {int} product(s)")
    public void the_wishlist_should_continue_with_products(int expectedQuantity) {
        the_wishlist_should_contain_products(expectedQuantity);
    }

    @Then("the product should be removed successfully")
    public void the_product_should_be_removed_successfully() {
        assertThat(currentException).isNull();
    }

    @Then("product {string} should not be present in the wishlist")
    public void product_should_not_be_present_in_the_wishlist(String productId) {
        try {
            wishlistService.checkProductExists(currentCustomerId, productId);
            fail("Product should not be present in wishlist");
        } catch (ProductNotFoundException e) {
            // Expected - product not found
        }
    }

    @Then("it should return the wishlist with {int} products")
    public void it_should_return_the_wishlist_with_products(int expectedQuantity) {
        assertThat(currentException).isNull();
        assertThat(currentWishlistResponse).isNotNull();
        assertThat(currentWishlistResponse.totalItems()).isEqualTo(expectedQuantity);
        assertThat(currentWishlistResponse.products()).hasSize(expectedQuantity);
    }

    @Then("it should include max limit information of {int} products")
    public void it_should_include_max_limit_information_of_products(int maxLimit) {
        assertThat(currentWishlistResponse.maxItems()).isEqualTo(maxLimit);
    }

    @Then("each product should have addition timestamp")
    public void each_product_should_have_addition_timestamp() {
        assertThat(currentWishlistResponse.products())
            .allMatch(product -> product.addedAt() != null);
    }

    @Then("it should return an empty wishlist")
    public void it_should_return_an_empty_wishlist() {
        it_should_return_the_wishlist_with_products(0);
    }

    @Then("it should include information of {int} products")
    public void it_should_include_information_of_products(int quantity) {
        assertThat(currentWishlistResponse.totalItems()).isEqualTo(quantity);
    }

    @Then("it should confirm the product exists")
    public void it_should_confirm_the_product_exists() {
        assertThat(currentException).isNull();
        assertThat(currentExistsResponse).isNotNull();
        assertThat(currentExistsResponse.exists()).isTrue();
        assertThat(currentExistsResponse.productId()).isEqualTo(currentProductId);
    }

    @Then("it should return the timestamp when it was added")
    public void it_should_return_the_timestamp_when_it_was_added() {
        assertThat(currentExistsResponse.addedAt()).isNotNull();
    }

    @Then("the wishlist should become empty")
    public void the_wishlist_should_become_empty() {
        the_wishlist_should_contain_products(0);
    }

    @Then("it should confirm it was cleared successfully")
    public void it_should_confirm_it_was_cleared_successfully() {
        assertThat(currentException).isNull();
    }

    @When("eu adiciono os seguintes produtos na wishlist:")
    public void eu_adiciono_os_seguintes_produtos_na_wishlist(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> product : products) {
            String productId = product.get("productId");
            try {
                wishlistService.addProduct(currentCustomerId, productId);
            } catch (Exception e) {
                this.currentException = e;
                break;
            }
        }
    }

    @When("eu tento adicionar o produto {string} novamente")
    public void eu_tento_adicionar_o_produto_novamente(String productId) {
        try {
            this.currentAddResponse = wishlistService.addProduct(currentCustomerId, productId);
            this.currentException = null;
        } catch (Exception e) {
            this.currentException = e;
        }
    }

    @When("eu tento adicionar o produto {string}")
    public void eu_tento_adicionar_o_produto(String productId) {
        eu_tento_adicionar_o_produto_novamente(productId);
    }

    @When("eu removo o produto {string}")
    public void eu_removo_o_produto(String productId) {
        this.currentProductId = productId;
        try {
            wishlistService.removeProduct(currentCustomerId, productId);
            this.currentException = null;
        } catch (Exception e) {
            this.currentException = e;
        }
    }

    @When("eu tento remover o produto {string}")
    public void eu_tento_remover_o_produto(String productId) {
        eu_removo_o_produto(productId);
    }

    @When("eu consulto a wishlist completa")
    public void eu_consulto_a_wishlist_completa() {
        try {
            this.currentWishlistResponse = wishlistService.getWishlist(currentCustomerId);
            this.currentException = null;
        } catch (Exception e) {
            this.currentException = e;
        }
    }

    @When("eu verifico se o produto {string} existe na wishlist")
    public void eu_verifico_se_o_produto_existe_na_wishlist(String productId) {
        this.currentProductId = productId;
        try {
            this.currentExistsResponse = wishlistService.checkProductExists(currentCustomerId, productId);
            this.currentException = null;
        } catch (Exception e) {
            this.currentException = e;
        }
    }

    @When("eu limpo a wishlist completamente")
    public void eu_limpo_a_wishlist_completamente() {
        try {
            wishlistService.clearWishlist(currentCustomerId);
            this.currentException = null;
        } catch (Exception e) {
            this.currentException = e;
        }
    }

    @When("eu tento {string} com customerId {string} e productId {string}")
    public void eu_tento_operacao_com_ids(String acao, String customerId, String productId) {
        try {
            switch (acao) {
                case "adicionar produto":
                    wishlistService.addProduct(customerId, productId);
                    break;
                case "remover produto":
                    wishlistService.removeProduct(customerId, productId);
                    break;
                case "consultar wishlist":
                    wishlistService.getWishlist(customerId);
                    break;
                default:
                    throw new IllegalArgumentException("Ação não reconhecida: " + acao);
            }
            this.currentException = null;
        } catch (Exception e) {
            this.currentException = e;
        }
    }

    @Then("o produto deve ser adicionado com sucesso")
    public void o_produto_deve_ser_adicionado_com_sucesso() {
        assertThat(currentException).isNull();
        assertThat(currentAddResponse).isNotNull();
        assertThat(currentAddResponse.productId()).isEqualTo(currentProductId);
        assertThat(currentAddResponse.customerId()).isEqualTo(currentCustomerId);
        assertThat(currentAddResponse.message()).contains("successfully");
    }

    @Then("a wishlist deve conter {int} produto(s)")
    public void a_wishlist_deve_conter_produtos(int quantidadeEsperada) {
        WishlistResponse response = wishlistService.getWishlist(currentCustomerId);
        assertThat(response.totalItems()).isEqualTo(quantidadeEsperada);
    }

    @Then("o produto {string} deve estar presente na wishlist")
    public void o_produto_deve_estar_presente_na_wishlist(String productId) {
        try {
            ProductExistsResponse response = wishlistService.checkProductExists(currentCustomerId, productId);
            assertThat(response.exists()).isTrue();
        } catch (ProductNotFoundException e) {
            fail("Produto deveria estar presente na wishlist");
        }
    }

    @Then("todos os produtos devem estar presentes na wishlist")
    public void todos_os_produtos_devem_estar_presentes_na_wishlist() {
        WishlistResponse response = wishlistService.getWishlist(currentCustomerId);
        assertThat(response.products()).isNotEmpty();
        assertThat(response.totalItems()).isGreaterThan(0);
    }

    @Then("deve retornar erro de {string}")
    public void deve_retornar_erro_de(String tipoErro) {
        assertThat(currentException).isNotNull();

        switch (tipoErro) {
            case "produto já existe":
                assertThat(currentException).isInstanceOf(ProductAlreadyExistsException.class);
                break;
            case "limite da wishlist excedido":
                assertThat(currentException).isInstanceOf(WishlistLimitExceededException.class);
                break;
            case "produto não encontrado":
                assertThat(currentException).isInstanceOf(ProductNotFoundException.class);
                break;
            case "ID do cliente inválido":
                assertThat(currentException).isInstanceOf(InvalidCustomerIdException.class);
                break;
            case "ID do produto inválido":
                assertThat(currentException).isInstanceOf(InvalidProductIdException.class);
                break;
            default:
                fail("Tipo de erro não reconhecido: " + tipoErro);
        }
    }

    @Then("a wishlist deve continuar com {int} produto(s)")
    public void a_wishlist_deve_continuar_com_produtos(int quantidadeEsperada) {
        a_wishlist_deve_conter_produtos(quantidadeEsperada);
    }

    @Then("o produto deve ser removido com sucesso")
    public void o_produto_deve_ser_removido_com_sucesso() {
        assertThat(currentException).isNull();
    }

    @Then("o produto {string} não deve estar presente na wishlist")
    public void o_produto_nao_deve_estar_presente_na_wishlist(String productId) {
        try {
            wishlistService.checkProductExists(currentCustomerId, productId);
            fail("Produto não deveria estar presente na wishlist");
        } catch (ProductNotFoundException e) {
            // Esperado - produto não encontrado
        }
    }

    @Then("deve retornar a wishlist com {int} produtos")
    public void deve_retornar_a_wishlist_com_produtos(int quantidadeEsperada) {
        assertThat(currentException).isNull();
        assertThat(currentWishlistResponse).isNotNull();
        assertThat(currentWishlistResponse.totalItems()).isEqualTo(quantidadeEsperada);
        assertThat(currentWishlistResponse.products()).hasSize(quantidadeEsperada);
    }

    @Then("deve incluir informação de limite máximo de {int} produtos")
    public void deve_incluir_informacao_de_limite_maximo_de_produtos(int limiteMaximo) {
        assertThat(currentWishlistResponse.maxItems()).isEqualTo(limiteMaximo);
    }

    @Then("cada produto deve ter timestamp de adição")
    public void cada_produto_deve_ter_timestamp_de_adicao() {
        assertThat(currentWishlistResponse.products())
            .allMatch(product -> product.addedAt() != null);
    }

    @Then("deve retornar a wishlist vazia")
    public void deve_retornar_a_wishlist_vazia() {
        deve_retornar_a_wishlist_com_produtos(0);
    }

    @Then("deve incluir informação de {int} produtos")
    public void deve_incluir_informacao_de_produtos(int quantidade) {
        assertThat(currentWishlistResponse.totalItems()).isEqualTo(quantidade);
    }

    @Then("deve confirmar que o produto existe")
    public void deve_confirmar_que_o_produto_existe() {
        assertThat(currentException).isNull();
        assertThat(currentExistsResponse).isNotNull();
        assertThat(currentExistsResponse.exists()).isTrue();
        assertThat(currentExistsResponse.productId()).isEqualTo(currentProductId);
    }

    @Then("deve retornar o timestamp de quando foi adicionado")
    public void deve_retornar_o_timestamp_de_quando_foi_adicionado() {
        assertThat(currentExistsResponse.addedAt()).isNotNull();
    }

    @Then("a wishlist deve ficar vazia")
    public void a_wishlist_deve_ficar_vazia() {
        a_wishlist_deve_conter_produtos(0);
    }

    @Then("deve confirmar que foi limpa com sucesso")
    public void deve_confirmar_que_foi_limpa_com_sucesso() {
        assertThat(currentException).isNull();
    }
}
