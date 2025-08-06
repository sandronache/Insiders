namespace Processing.Models;

public class ProcessingRequest
{
    public string FileName { get; set; }
    public List<OperationRequest> Operations { get; set; }
    public string ImageBase64 { get; set; }

}
