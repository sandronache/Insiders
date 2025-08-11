using SixLabors.ImageSharp;
using SixLabors.ImageSharp.PixelFormats;

namespace Shared.Models;

public class ProcessingResult
{
    public bool Succes { get; }
    public string Message { get; }
    public Image<Rgba32> Image { get; }

    public ProcessingResult(bool success, string message, Image<Rgba32> image)
    {
        Succes = Succes;
        Message = message;
        Image = image;
    }
}