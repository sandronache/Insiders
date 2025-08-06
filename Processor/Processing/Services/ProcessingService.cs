namespace Processing.Services;

using System;
using Processing.Interfaces;
using Processing.Models;
using Processing.Operations;
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.Formats.Png;
using Processing.Validators;

public class ProcessingService
{
    private readonly ImageOperationFactory _factory = new();
    private readonly ValidationService _validator;
    public Image currentImage { get; set; }

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
        currentImage = LoadFromBase64(request.ImageBase64);

        _validator.Validate(currentImage);
        if (_validator.ErrorMessage != null)
        {
            return new ProcessingResult
            {
                Response = false,
                Message = _validator.ErrorMessage,
                FileName = request.FileName,
                ImageBase64 = null
            };
        } 
        
        foreach (var op in request.Operations)
        {
            Enum.TryParse<OperationType>(op.Name, ignoreCase: true, out var operationName);
            var operation = _factory.GetOperation(operationName);
            operation.Apply(currentImage, op.Parameters);
        }

        using var ms = new MemoryStream();
        currentImage.Save(ms, new PngEncoder());
        byte[] Bytes = ms.ToArray();


        return new ProcessingResult
        {
            Response = true,
            Message = "No errors",
            FileName = request.FileName,
            ImageBase64 = Convert.ToBase64String(Bytes)
        };
    }

    public Image LoadFromBase64(string imageBase64)
    {
        byte[] imageBytes = Convert.FromBase64String(imageBase64);
        using MemoryStream inputStream = new MemoryStream(imageBytes);
        return Image.Load(inputStream);
    }
}