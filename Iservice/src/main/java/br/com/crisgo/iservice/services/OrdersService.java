package br.com.crisgo.iservice.services;

import br.com.crisgo.iservice.DTO.request.RequestOrdersDTO;
import br.com.crisgo.iservice.DTO.response.ResponseOrdersDTO;
import br.com.crisgo.iservice.controllers.OrdersController;
import br.com.crisgo.iservice.controllers.UserController;
import br.com.crisgo.iservice.exceptions.EntityNotFoundException;
import br.com.crisgo.iservice.mapper.DozerMapper;
import br.com.crisgo.iservice.models.*;
import br.com.crisgo.iservice.repositorys.OrdersRepository;
import br.com.crisgo.iservice.repositorys.ProductRepository;
import br.com.crisgo.iservice.repositorys.SellerRepository;
import br.com.crisgo.iservice.repositorys.UserRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;

    @Autowired
    public OrdersService(OrdersRepository ordersRepository,
                         UserRepository userRepository,
                         ProductRepository productRepository,
                         SellerRepository sellerRepository) {
        this.ordersRepository = ordersRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.sellerRepository = sellerRepository;
    }

    public ResponseOrdersDTO createOrder(Long userId, Long productId, Long sellerId, RequestOrdersDTO requestOrdersDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario não encontrado"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Vendedor não encontrado"));

        Orders orders = DozerMapper.parseObject(requestOrdersDTO, Orders.class);
        orders.setUser(user);
        orders.setProduct(product);
        orders.setSeller(seller);

        Orders savedOrder = ordersRepository.save(orders);
        return addHateoasLinks(DozerMapper.parseObject(savedOrder, ResponseOrdersDTO.class));
    }

    public List<ResponseOrdersDTO> findByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario não encontrado"));

        List<Orders> orders = ordersRepository.findByUser(user);

        if (orders.isEmpty()) {
            throw new EntityNotFoundException("Nenhuma compra feita por esse usuario foi encontrada");
        }

        return orders.stream()
                .map(order -> addHateoasLinks(DozerMapper.parseObject(order, ResponseOrdersDTO.class)))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteById(Long id) {
        if (!ordersRepository.existsById(id)) {
            throw new EntityNotFoundException("Produto de ID " + id + " não encontrado");
        }
        ordersRepository.deleteById(id);
    }

    @Transactional
    public ResponseOrdersDTO updateOrder(Long id, RequestOrdersDTO requestOrdersDTO) {
        Orders existingOrder = ordersRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Compra de ID " + id + " não encontrado"));

        DozerMapper.mapOntoExistingObject(requestOrdersDTO, existingOrder);

        Orders updatedOrder = ordersRepository.save(existingOrder);
        return addHateoasLinks(DozerMapper.parseObject(updatedOrder, ResponseOrdersDTO.class));
    }

    private ResponseOrdersDTO addHateoasLinks(ResponseOrdersDTO ordersDTO) {
        Link selfLink = linkTo(methodOn(OrdersController.class).findOrdersByUser(ordersDTO.getId())).withSelfRel();
        Link updateLink = linkTo(methodOn(OrdersController.class).updateOrder(ordersDTO.getId(), null)).withRel("update");
        Link deleteLink = linkTo(methodOn(OrdersController.class).deleteOrder(ordersDTO.getId())).withRel("delete");

        ordersDTO.add(selfLink);
        ordersDTO.add(updateLink);
        ordersDTO.add(deleteLink);

        return ordersDTO;
    }
}
