package com.example.demo1;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;



public class CrptApi {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private int requestLimit;
    private long timeInterval;
    private int requestCount;
    private long lastResetTime;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.requestLimit = requestLimit;
        this.timeInterval = timeUnit.toMillis(1);
        this.requestCount = 0;
        this.lastResetTime = System.currentTimeMillis();
    }

    public synchronized void createDocument(Document document, String signature) throws Exception {
        System.out.println("Запускаемся...");
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastResetTime > timeInterval) {
            requestCount = 0;
            lastResetTime = currentTime;
        }
        while (requestCount >= requestLimit) {
            System.out.println("Ждем...");
            Thread.sleep(100);
            currentTime = System.currentTimeMillis();
            if (currentTime - lastResetTime > timeInterval) {
                requestCount = 0;
                lastResetTime = currentTime;
            }
        }
        System.out.println("Создаем запрос...");
        requestCount++;
        String jsonBody = objectMapper.writeValueAsString(document);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://ismp.crpt.ru/api/v3/lk/documents/create"))
                .header("Content-Type", "application/json")
                .header("Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        System.out.println("Отправляем ответ...");
        try {

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response code: " + response.statusCode());
            System.out.println("Response bode: " + response.body());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
// внутр классы
    public static class Document {
        public Description description;
        public String doc_id;
        public String doc_status;
        public String doc_type = "LP_INTRODUCE_GOODS";
        public boolean importRerequest;
        public String owner_inn;
        public String producer_inn;
        public String production_date;
        public String prodaction_type;
        public Products products;
        public String reg_date;
        public String reg_number;


    public static class Description {
        public String participantinn;

    }

    public static class Products {
        public String certificate_document;
        public String  certificate_document_date;
        public String certificate_document_number;
        public String owner_inn;
        public String producer_inn;
        public String production_date;
        public String tnved_code;
        public String uit_code;
        public String uitu_code;
    }

}
// главный метод
    public static void main(String[] args) throws Exception {
        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS,5);

        Document.Description description = new Document.Description();
        description.participantinn = "1234567890";

        Document.Products products = new Document.Products();
        products.certificate_document = "cert_doc";
        products.certificate_document_number = "123456";
        products.certificate_document_date = "2020-01-23";
        products.production_date = "2020-01-23";
        products.tnved_code = "0101";
        products.uit_code = "12345678901234";
        products.owner_inn = "1234567890";
        products.uitu_code = "123456789012345";
        products.producer_inn = "0987654321";

        Document document = new Document();
        document.description = description;
        document.products = products;
        document.doc_id = "doc_id";
        document.production_date = "2020-01-23";
        document.doc_status = "NEW";
        document.doc_type = "TYPE";
        document.importRerequest = true;
        document.owner_inn = "1234567890";
        document.prodaction_type = "TYPE";
        document.producer_inn = "0987654321";
        document.reg_date = "2020-01-23";
        document.reg_number = "reg_number";

        crptApi.createDocument(document, "signature");
    }

}
