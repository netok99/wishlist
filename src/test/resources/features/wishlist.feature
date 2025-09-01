@wishlist
Feature: Customer Wishlist Management
  As an e-commerce customer
  I want to manage my wishlist
  So that I can save products I'm interested in purchasing

  Background:
    Given the wishlist system is working
    And there is a customer with ID "customer123"

  @add-product
  Scenario: Add product to empty wishlist
    Given the customer "customer123" has an empty wishlist
    When I add product "product456" to the wishlist
    Then the product should be added successfully
    And the wishlist should contain 1 product
    And product "product456" should be present in the wishlist

  @add-product
  Scenario: Add multiple products to wishlist
    Given the customer "customer123" has an empty wishlist
    When I add the following products to the wishlist:
      | productId  |
      | product001 |
      | product002 |
      | product003 |
    Then the wishlist should contain 3 products
    And all products should be present in the wishlist

  @add-product @business-rule
  Scenario: Try to add duplicate product to wishlist
    Given the customer "customer123" wishlist contains product "product456"
    When I try to add product "product456" again
    Then it should return "product already exists" error
    And the wishlist should continue with 1 product

  @add-product @business-rule
  Scenario: Try to add product when wishlist is at limit
    Given the customer "customer123" already has 20 products in wishlist
    When I try to add product "product999"
    Then it should return "wishlist limit exceeded" error
    And the wishlist should continue with 20 products

  @remove-product
  Scenario: Remove existing product from wishlist
    Given the customer "customer123" wishlist contains the products:
      | productId  |
      | product001 |
      | product002 |
      | product003 |
    When I remove product "product002"
    Then the product should be removed successfully
    And the wishlist should contain 2 products
    And product "product002" should not be present in the wishlist

  @remove-product
  Scenario: Try to remove product that doesn't exist in wishlist
    Given the customer "customer123" wishlist contains product "product456"
    When I try to remove product "product999"
    Then it should return "product not found" error
    And the wishlist should continue with 1 product

  @get-wishlist
  Scenario: Query wishlist with products
    Given the customer "customer123" wishlist contains the products:
      | productId  |
      | product001 |
      | product002 |
      | product003 |
    When I query the complete wishlist
    Then it should return the wishlist with 3 products
    And it should include max limit information of 20 products
    And each product should have addition timestamp

  @get-wishlist
  Scenario: Query empty wishlist
    Given the customer "customer123" has an empty wishlist
    When I query the complete wishlist
    Then it should return an empty wishlist
    And it should include information of 0 products
    And it should include max limit information of 20 products

  @check-product
  Scenario: Check if product exists in wishlist
    Given the customer "customer123" wishlist contains product "product456"
    When I check if product "product456" exists in the wishlist
    Then it should confirm the product exists
    And it should return the timestamp when it was added

  @check-product
  Scenario: Check product that doesn't exist in wishlist
    Given the customer "customer123" has an empty wishlist
    When I check if product "product999" exists in the wishlist
    Then it should return "product not found" error

  @clear-wishlist
  Scenario: Clear wishlist with products
    Given the customer "customer123" has 5 products in wishlist
    When I clear the wishlist completely
    Then the wishlist should become empty
    And it should confirm it was cleared successfully

  @validation
  Scenario Outline: Validate invalid IDs
    Given the system is working
    When I try to <action> with customerId "<customerId>" and productId "<productId>"
    Then it should return "<expected_error>" error
    Examples:
      | action         | customerId  | productId  | expected_error      |
      | add product    | ""          | product123 | invalid customer ID |
      | add product    | customer123 | ""         | invalid product ID  |
      | remove product | null        | product123 | invalid customer ID |
      | query wishlist | " "         | -          | invalid customer ID |
