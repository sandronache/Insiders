using SixLabors.ImageSharp;

namespace Shared.Models;

public class ProcessingRequest
{
    public Image Image { get; }
    public List<OperationDescriptor> OperationsDescriptors { get; }

    public ProcessingRequest(Image image, List<OperationDescriptor> descriptors)
    {
        Image = image;
        OperationsDescriptors = descriptors;
    }
}
