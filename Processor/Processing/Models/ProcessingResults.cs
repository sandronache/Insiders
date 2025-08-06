namespace Processing.Models;

public class ProcessingResult
{
    public bool Response { get; set; }
    public string Message { get; set; }
    public string FileName { get; set; }
    public string ImageBase64 { get; set; }
}