using SixLabors.ImageSharp;

namespace Shared.Models;

public class ProcessingResult
{
    public bool Succes { get; }
    public string Message { get; }
    public Image Image { get; }

    public ProcessingResult(bool success, string message, Image image)
    {
        Succes = Succes;
        Message = message;
        Image = image;
    }
}