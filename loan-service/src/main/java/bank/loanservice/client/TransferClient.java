package bank.loanservice.client;

import io.github.oguzalpcepni.dto.transferdto.TransferRequest;
import io.github.oguzalpcepni.dto.transferdto.TransferResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "transfer-service")
public interface TransferClient {

    @PostMapping("/api/v1/transfers")
    ResponseEntity<TransferResponse> initiateTransfer(@RequestBody TransferRequest transferRequest);
}
