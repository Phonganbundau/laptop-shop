package vn.hoidanit.laptopshop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import vn.hoidanit.laptopshop.domain.Cart;
import vn.hoidanit.laptopshop.domain.CartDetail;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.repository.CartDetailRepository;
import vn.hoidanit.laptopshop.repository.CartRepository;
import vn.hoidanit.laptopshop.repository.ProductRepository;

@Service
public class ProductService {
  private final ProductRepository productRepository;
  private final CartRepository cartRepository;
  private final CartDetailRepository cartDetailRepository;

  private final UserService userService;

  public ProductService(ProductRepository productRepository, CartRepository cartRepository,
      CartDetailRepository cartDetailRepository, UserService userService) {
    this.productRepository = productRepository;
    this.cartRepository = cartRepository;
    this.cartDetailRepository = cartDetailRepository;
    this.userService = userService;
  }

  public List<Product> getAllProducts() {
    return this.productRepository.findAll();
  }

  public Optional<Product> getProductById(long id) {
    return this.productRepository.findById(id);
  }

  public Product createProduct(Product product) {
    return this.productRepository.save(product);
  }

  public void deleteProduct(long id) {
    this.productRepository.deleteById(id);
  }

  public void handleAddProductToCart(String email, long productId, HttpSession session) {
    User user = this.userService.getUserByEmail(email);
    if (user != null) {
      Cart cart = this.cartRepository.findByUser(user);
      if (cart == null) {
        // tạo mới cart
        Cart otherCart = new Cart();
        otherCart.setUser(user);
        otherCart.setSum(0);

        cart = this.cartRepository.save(otherCart);
      }

      // save car detail
      // tìm product by ID

      Optional<Product> productOptional = this.productRepository.findById(productId);
      if (productOptional.isPresent()) {
        Product realProduct = productOptional.get();
        // check sản phẩm đã có trong giỏ hàng trước đây chưa?
        CartDetail oldDetail = this.cartDetailRepository.findByCartAndProduct(cart, realProduct);

        if (oldDetail == null) {
          CartDetail cartDetail = new CartDetail();
          cartDetail.setCart(cart);
          cartDetail.setProduct(realProduct);
          cartDetail.setPrice(realProduct.getPrice());
          cartDetail.setQuantity(1);
          this.cartDetailRepository.save(cartDetail);

          // update cart sum
          int s = cart.getSum() + 1;
          cart.setSum(cart.getSum() + 1);
          cart = this.cartRepository.save(cart);
          session.setAttribute("sum", s);
        } else {
          oldDetail.setQuantity(oldDetail.getQuantity() + 1);
          this.cartDetailRepository.save(oldDetail);
        }
      }
    }
  }

  public Cart fetchByUser(User user) {
    return this.cartRepository.findByUser(user);
  }

}