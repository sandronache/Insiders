using Processing.Interfaces;
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.PixelFormats;

namespace Processing.Services;

public class ValidationService
{
    private readonly List<IImageValidator> _validators;
    public string ErrorMessage { get; set; }

    public ValidationService(List<IImageValidator> validators)
    {
        _validators = validators;
        ErrorMessage = null;
    }

    public bool Validate(Image<Rgba32> image)
    {
        foreach (IImageValidator validator in _validators)
        {
            if (!validator.Validate(image))
            {
                ErrorMessage = validator.GetErrorMessage();
                return false;
            }
        }
        return true;
    }
}
