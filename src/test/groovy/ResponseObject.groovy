import com.fasterxml.jackson.annotation.JsonProperty

class ResponseObject {
    def id
    def name
    Data data
}

class Data {
    def color
    def capacity
    @JsonProperty("CPU model")
    def cpuModel
    def year
    def price
    @JsonProperty("Hard disk size")
    def hardDiskSize
}

class ErrorObject {
    def error
}

class InvalidRequestObject {
    def id
    @JsonProperty("Invalid field")
    def invalidField
    @JsonProperty("Random field")
    def randomField
}