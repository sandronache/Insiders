namespace Processing.Models;

public class OperationRequest
{
    public string Type { get; set; }
    public string Name { get; set; }
    public Dictionary<string, string> Parameters { get; set; }
}