package br.com.crisgo.iservice.services;

import br.com.crisgo.iservice.DTO.request.RequestSellerDTO;
import br.com.crisgo.iservice.DTO.response.ResponseSellerDTO;
import br.com.crisgo.iservice.controllers.SellerController;
import br.com.crisgo.iservice.exceptions.EntityNotFoundException;
import br.com.crisgo.iservice.models.Seller;
import br.com.crisgo.iservice.repositorys.SellerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;
import br.com.crisgo.iservice.mapper.Mapper;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class SellerService {

    private final SellerRepository sellerRepository;
    private final Mapper modelMapper;
    @Autowired
    public SellerService(SellerRepository sellerRepository, Mapper modelMapper) {
        this.sellerRepository = sellerRepository;
        this.modelMapper = modelMapper;
    }

    public List<ResponseSellerDTO> findAll() {
        List<Seller> sellers = sellerRepository.findAll();
        return modelMapper.map(sellers, ResponseSellerDTO.class);
    }

    public ResponseSellerDTO findById(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario de ID " + id + " não encontrado"));
        ResponseSellerDTO responseSellerDTO = modelMapper.map(seller, ResponseSellerDTO.class);
        addHateoasLinks(responseSellerDTO);
        return responseSellerDTO;
    }

    public ResponseSellerDTO createSeller(RequestSellerDTO requestSellerDTO) {
        // Map RequestSellerDTO to Seller entity
        Seller seller = modelMapper.map(requestSellerDTO, Seller.class);
        Seller savedSeller = sellerRepository.save(seller);
        // Map saved Seller entity to ResponseSellerDTO
        ResponseSellerDTO responseSellerDTO = modelMapper.map(seller, ResponseSellerDTO.class);
        addHateoasLinks(responseSellerDTO);
        return responseSellerDTO;
    }

    @Transactional
    public ResponseSellerDTO updateSeller(Long id, RequestSellerDTO requestSellerDTO) {
        Seller existingSeller = sellerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario de ID " + id + " não encontrado"));

        // Map fields from requestSellerDTO onto existingSeller
        modelMapper.mapOntoExistingObject(requestSellerDTO, existingSeller);

        Seller updatedSeller = sellerRepository.save(existingSeller);
        ResponseSellerDTO responseSellerDTO = modelMapper.map(updatedSeller, ResponseSellerDTO.class);
        addHateoasLinks(responseSellerDTO);
        return responseSellerDTO;
    }

    @Transactional
    public void deleteById(Long id) {
        if (!sellerRepository.existsById(id)) {
            throw new EntityNotFoundException("Usuario de ID " + id + " não encontrado");
        }
        sellerRepository.deleteById(id);
    }

    private void addHateoasLinks(ResponseSellerDTO sellerDTO) {
        Link selfLink = linkTo(methodOn(SellerController.class).getSeller(sellerDTO.getId())).withSelfRel();
        Link updateLink = linkTo(methodOn(SellerController.class).updateSeller(sellerDTO.getId(), null)).withRel("update");
        Link deleteLink = linkTo(methodOn(SellerController.class).deleteSeller(sellerDTO.getId())).withRel("delete");

        sellerDTO.add(selfLink);
        sellerDTO.add(updateLink);
        sellerDTO.add(deleteLink);
    }
}
