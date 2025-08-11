using SixLabors.ImageSharp;

namespace Processing.Interfaces;

public interface IImageValidator
{
    public string GetErrorMessage();
    public bool Validate(Image image);
}