import groovy.util.logging.Slf4j
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.web.client.HttpClientErrorException
import spock.lang.Shared
import spock.lang.Specification
import org.springframework.web.client.RestClient;

@Slf4j
class RestApiDevTest extends Specification {
    @Shared
    def restClient = RestClient.create();

    @Shared
    def BASEURL = "https://api.restful-api.dev/objects";


    def "GET - list all objects returns expected results"() {
        given:
            def responseObject
            def statusCode

        when:
            def response = restClient.get().uri(BASEURL).retrieve().toEntity(ResponseObject[].class)
            statusCode = response.getStatusCode()
            responseObject = response.getBody()

        then:
            statusCode == HttpStatusCode.valueOf(200)

            responseObject[0].id == "1"
            responseObject[0].name == "Google Pixel 6 Pro"
            responseObject[0].data.color == "Cloudy White"
            responseObject[0].data.capacity == "128 GB"
            // assume more checked, 13 is too long
            responseObject.size() == 13
    }

    def "GET - list objects by multiple IDs returns expected results"() {
        given:
            def responseObject
            def statusCode

        when:
            def response = restClient.get().uri(BASEURL + "?id=1&id=4&id=7").retrieve().toEntity(ResponseObject[].class)
            statusCode = response.getStatusCode()
            responseObject = response.getBody()

        then:
            statusCode == HttpStatusCode.valueOf(200)

            responseObject[0].id == "1"
            responseObject[0].name == "Google Pixel 6 Pro"
            responseObject[0].data.color == "Cloudy White"
            responseObject[0].data.capacity == "128 GB"

            responseObject[1].id == "4"
            responseObject[1].name == "Apple iPhone 11, 64GB"
            responseObject[1].data.color == "Purple"
            responseObject[1].data.price == 389.99d

            responseObject[2].id == "7"
            responseObject[2].name == "Apple MacBook Pro 16"
            responseObject[2].data.year == 2019
            responseObject[2].data.price == 1849.99d
            responseObject[2].data.cpuModel == "Intel Core i9"
            responseObject[2].data.hardDiskSize == "1 TB"

            responseObject.size() == 3
    }

    def "GET - list objects by multiple IDs that don't exist returns empty"() {
        given:
            def responseObject
            def statusCode

        when:
            def response = restClient.get().uri(BASEURL + "?id=invalid1&id=invalid2&id=invalid3").retrieve().toEntity(ResponseObject[].class)
            statusCode = response.getStatusCode()
            responseObject = response.getBody()

        then:
            statusCode == HttpStatusCode.valueOf(200)

            responseObject.size() == 0
    }

    def "GET - single object returns expected results"() {
        given:
            def responseObject
            def statusCode

        when:
            def response = restClient.get().uri(BASEURL + "/${id}").retrieve().toEntity(ResponseObject.class)
            statusCode = response.getStatusCode()
            responseObject = response.getBody()

        then:
            statusCode == HttpStatusCode.valueOf(200)

            responseObject.id == id
            responseObject.name == name
            responseObject.data?.color == color
            responseObject.data?.capacity == capacity
            responseObject.data?.cpuModel == cpuModel
            responseObject.data?.year == year
            responseObject.data?.price == price

        where:
            id | name | color | capacity | cpuModel | year | price | hardDiskSize
            "1" | "Google Pixel 6 Pro" | "Cloudy White" | "128 GB" | null | null | null | null
            "2" | "Apple iPhone 12 Mini, 256GB, Blue" | null | null | null | null | null | null
            "3" | "Apple iPhone 12 Pro Max" | "Cloudy White" | null | null | null | null | null
            "7" | "Apple MacBook Pro 16" | null | null | "Intel Core i9" | 2019 | 1849.99d | "1 TB"
    }

    def "GET - single object with invalid ID returns expected error"() {
        given:
            def responseObject
            def statusCode

        when:
            try {
                def response = restClient.get().uri(BASEURL + "/invalidId").retrieve().toEntity(ErrorObject.class)
                statusCode = response.getStatusCode()
                responseObject = response.body
            } catch (HttpClientErrorException.NotFound e) {
                responseObject = e.getResponseBodyAs(ErrorObject.class)
                statusCode = e.statusCode
            }

        then:
            responseObject.error == "Oject with id=invalidId was not found."
            statusCode == HttpStatusCode.valueOf(404)
    }

    def "POST - add object creates a new object as expected"() {
        given:
            def testDataObject = new Data()
            testDataObject.color = color
            testDataObject.capacity = capacity
            testDataObject.cpuModel = cpuModel
            testDataObject.year = year
            testDataObject.price = price
            def testObject = new ResponseObject()

            testObject.name = name
            testObject.data = testDataObject

            def createResponse = restClient.post().uri(BASEURL).contentType(MediaType.APPLICATION_JSON).body(testObject).retrieve().toEntity(ResponseObject.class)
            def createStatusCode = createResponse.getStatusCode()
            String id = createResponse.body.id
            def retrieveResponseObject
            def retrieveStatusCode

        when:
            def retrieveResponse = restClient.get().uri(BASEURL + "/{id}", id).retrieve().toEntity(ResponseObject.class)
            retrieveStatusCode = retrieveResponse.getStatusCode()
            retrieveResponseObject = retrieveResponse.body

        then:
            createStatusCode == HttpStatusCode.valueOf(200)
            retrieveStatusCode == HttpStatusCode.valueOf(200)

            retrieveResponseObject.id == id
            retrieveResponseObject.name == name
            retrieveResponseObject.data.color == color
            retrieveResponseObject.data.capacity == capacity
            retrieveResponseObject.data.cpuModel == cpuModel
            retrieveResponseObject.data.year == year
            retrieveResponseObject.data.price == price

        where:
            name | color | capacity | cpuModel | year | price
            "Google Computer" | "Silver" | "256 GB" | "Intel Core i9" | null | null
            "Google Pixel 6 Pro" | "Cloudy White" | null | null | 2017 | null
            "Apple iPhone 12 Mini, 256GB, Blue" | null | "256GB" | null | null | 1499.99d
            "Apple iPhone 12 Pro Max" | "Cloudy White" | "128GB" | null | 2022 | null
            "Apple MacBook Pro 16" | null | "1 TB" | "Apple M3" | 2019 | 1849.99d
    }

    def "POST - error response for invalid fields"() {

        // this API allows creates with invalid fields/incorrect formats, it just only checks the fields that exist and creates object with those
        // as a result an empty null object is returned back with only an ID

        given:
            def testObject = new InvalidRequestObject()

            testObject.invalidField = "invalid field"
            testObject.randomField = "random field that doesn't exist"
            def retrieveResponseObject
            def retrieveStatusCode
            def createStatusCode


        when:
            def createResponse = restClient.post().uri(BASEURL).contentType(MediaType.APPLICATION_JSON).body(testObject).retrieve().toEntity(ResponseObject.class)
            createStatusCode = createResponse.getStatusCode()
            String id = createResponse.body.id

            def retrieveResponse = restClient.get().uri(BASEURL + "/{id}", id).retrieve().toEntity(ResponseObject.class)
            retrieveStatusCode = retrieveResponse.getStatusCode()
            retrieveResponseObject = retrieveResponse.body

        then:
            createStatusCode == HttpStatusCode.valueOf(200)
            retrieveStatusCode == HttpStatusCode.valueOf(200)

            retrieveResponseObject.id == id
            retrieveResponseObject.name == null
            retrieveResponseObject.data == null
    }

    def "POST - error response for incorrect body format"() {
        given:
            def testObject = "this is an invalid object"

            def responseObject
            def createStatusCode

        when:
            try {
                def createResponse = restClient.post().uri(BASEURL).contentType(MediaType.APPLICATION_JSON).body(testObject).retrieve().toEntity(ResponseObject.class)
                createStatusCode = createResponse.getStatusCode()
                responseObject = createResponse.body
            } catch (HttpClientErrorException.BadRequest e) {
                responseObject = e.getResponseBodyAs(ErrorObject)
                createStatusCode = e.getStatusCode()
            }

        then:
            createStatusCode == HttpStatusCode.valueOf(400)

            responseObject.error == "400 Bad Request. If you are trying to create or update the data, potential issue is that you are sending incorrect body json or it is missing at all."
    }

    def "POST - delete object creates an object as expected"() {
        given:
            def testObject = new ResponseObject()

            testObject.name = "To be deleted object"

            def retrieveResponseObject
            def retrieveStatusCode
            def deleteStatusCode
            def afterDeleteResponseObject
            def afterDeleteStatusCode
            def id

        when:
            def createResponse = restClient.post().uri(BASEURL).contentType(MediaType.APPLICATION_JSON).body(testObject).retrieve().toEntity(ResponseObject.class)
            id = createResponse.body.id

            def retrieveResponse = restClient.get().uri(BASEURL + "/{id}", id).retrieve().toEntity(ResponseObject.class)
            retrieveStatusCode = retrieveResponse.getStatusCode()
            retrieveResponseObject = retrieveResponse.body

            def deleteResponse = restClient.delete().uri(BASEURL + "/{id}", id).retrieve().toEntity(ResponseObject.class)
            deleteStatusCode = deleteResponse.getStatusCode()

            try {
                def afterDeleteResponse = restClient.get().uri(BASEURL + "/{id}", id).retrieve().toEntity(ErrorObject.class)
                afterDeleteStatusCode = afterDeleteResponse.getStatusCode()
                afterDeleteResponseObject = afterDeleteResponse.body
            } catch (HttpClientErrorException.NotFound e) {
                afterDeleteResponseObject = e.getResponseBodyAs(ErrorObject.class)
                afterDeleteStatusCode = e.statusCode
            }

        then:
            retrieveStatusCode == HttpStatusCode.valueOf(200)
            retrieveResponseObject.id == id
            retrieveResponseObject.name == "To be deleted object"

            deleteStatusCode == HttpStatusCode.valueOf(200)

            afterDeleteResponseObject.error == "Oject with id=${id} was not found."
            afterDeleteStatusCode == HttpStatusCode.valueOf(404)
    }

    def "POST - delete object errors when object doesn't exist"() {
        given:
            def deleteStatusCode
            def deleteResponseObject

        when:
            try {
                def deleteResponse = restClient.delete().uri(BASEURL + "/invalidId").retrieve().toEntity(ResponseObject.class)
                deleteStatusCode = deleteResponse.getStatusCode()
                deleteResponseObject = deleteResponse.body
            } catch (HttpClientErrorException.NotFound e) {
                deleteResponseObject = e.getResponseBodyAs(ErrorObject.class)
                deleteStatusCode = e.statusCode
            }

        then:
            deleteResponseObject.error == "Object with id = invalidId doesn't exist."
            deleteStatusCode == HttpStatusCode.valueOf(404)
    }
}