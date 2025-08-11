using Shared.Enums;

namespace Shared.Models;

public class OperationDescriptor
{
    public OperationType Type { get; set; }
    public Dictionary<string, string> Parameters { get; set; }

    public OperationDescriptor() {}
}