using SixLabors.ImageSharp;
using SixLabors.ImageSharp.PixelFormats;

namespace Processing.Interfaces;

public interface IImageValidator
{
    public string GetErrorMessage();
    public bool Validate(Image image);
}