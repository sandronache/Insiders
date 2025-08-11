namespace Processing.Services;

using System;
using Processing.Interfaces;
using Processing.Operations;
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.Formats.Png;
using Processing.Validators;
using Shared.Models;

public class ProcessingService
{
    private readonly OperationFactory _factory = new();
    private readonly ValidationService _validator;

    public ProcessingService()
    {
        _validator = new ValidationService(new List<IImageValidator>
        {
            new SizeValidator(),
            new FormatValidator()
        });
    }

    public ProcessingResult Process(ProcessingRequest request)
    {
        Image currentImage = request.Image;

        foreach (OperationDescriptor descriptor in request.OperationsDescriptors)
        {
            IImageOperation operation = _factory.GetOperation(descriptor.Type);
            operation.Apply(currentImage, descriptor.Parameters);
        }

        return new ProcessingResult(true, "Processed", currentImage);
    }
}