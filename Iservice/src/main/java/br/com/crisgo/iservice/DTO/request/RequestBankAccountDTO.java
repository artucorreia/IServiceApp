package br.com.crisgo.iservice.DTO.request;

import br.com.crisgo.iservice.models.Seller;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestBankAccountDTO {

    @NotBlank(message = "Numero da conta é obrigatorio")
    private String name;

    @NotBlank(message = "Agência é obrigatorio")
    private String agency;

    @NotBlank(message = " Estar ligado a um vendedor é obrigatorio")
    private Seller seller;

}
