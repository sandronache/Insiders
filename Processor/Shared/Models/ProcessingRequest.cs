using SixLabors.ImageSharp;
using SixLabors.ImageSharp.PixelFormats;

namespace Shared.Models;

public class ProcessingRequest
{
    public Image<Rgba32> Image { get; }
    public List<OperationDescriptor> OperationsDescriptors { get; }

    public ProcessingRequest(Image<Rgba32> image, List<OperationDescriptor> descriptors)
    {
        Image = image;
        OperationsDescriptors = descriptors;
    }
}
