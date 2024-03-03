import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {
    private final static String URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final Gson gson;
    private final HttpClient httpClient;
    private final int requestLimit;
    private final AtomicInteger requestCounter;
    private final Duration delay;
    private final ScheduledExecutorService executorService;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.gson = new Gson();
        this.httpClient = HttpClient.newHttpClient();
        this.requestLimit = requestLimit;
        this.requestCounter = new AtomicInteger(0);
        this.delay = Duration.ofMillis(timeUnit.toMillis(1) / requestLimit);
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void postDocument(Document document, String signature)
            throws IOException, InterruptedException {
        int temp = requestCounter.incrementAndGet();
        executorService.schedule(requestCounter::decrementAndGet, delay.toMillis() * temp, TimeUnit.MICROSECONDS);
        if (temp >= requestLimit) {
            Thread.sleep(delay.toMillis() * temp);
        }
        sendRequest(document, signature);
    }

    private void sendRequest(Document document, String signature)
            throws IOException, InterruptedException {
        String body = gson.toJson(document);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .header("Signature", signature)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() != 200) {
            throw new RuntimeException();
        }
    }

    @Data
    @AllArgsConstructor
    public class Document {
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private LocalDate production_date;
        private String production_type;
        private List<Product> products;
        private LocalDate reg_date;
        private String reg_number;

        public Document(int doc) {
            this.doc_id = String.valueOf(doc);
        }

        @Data
        @AllArgsConstructor
        @Builder
        public static class Description {
            private String participantInn;
        }
    }

    @Data
    @AllArgsConstructor
    public class Product {
        private String certificate_document;
        private LocalDate certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private LocalDate production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;
    }
}
