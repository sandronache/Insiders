namespace Processing.Services;

using System;
using Processing.Models;
using Processing.Operations;
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.Formats.Png;

public class ProcessingService
{
    private readonly ImageOperationFactory _factory = new();

    public void Process(ProcessingRequest request, ProcessingResult result)
    {
        ImagePackage imagePackage = new ImagePackage(request.ImageBase64);

        foreach (var op in request.Operations)
        {
            Enum.TryParse<OperationType>(op.Name, ignoreCase: true, out var operationName);
            var operation = _factory.GetOperation(operationName);
            operation?.Apply(imagePackage, op.Parameters);
        }

        using var ms = new MemoryStream();
        imagePackage.Image.Save(ms, new PngEncoder());
        byte[] Bytes = ms.ToArray();

        result.ImageBase64 = Convert.ToBase64String(Bytes);
    }
}