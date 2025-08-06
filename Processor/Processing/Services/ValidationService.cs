using Processing.Interfaces;
using SixLabors.ImageSharp;

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

    public void Validate(Image image)
    {
        foreach (IImageValidator validator in _validators)
        {
            if (!validator.Validate(image))
            {
                ErrorMessage = validator.GetErrorMessage();
            }
        }
    }
}
