using Shared.Enums;

namespace Processing.Operations;
using Processing.Interfaces;

public class OperationFactory
{
    private readonly Dictionary<OperationType, IImageOperation> _operations;

    public OperationFactory()
    {
        _operations = new Dictionary<OperationType, IImageOperation>()
        {
            { OperationType.BlurFilter, new BlurFilter() },
            { OperationType.BrightnessFilter, new BrightnessFilter() },
            { OperationType.DoNothingFilter, new DoNothingFilter() },
            { OperationType.GrayscaleFilter, new GrayscaleFilter() },
            { OperationType.InvertFilter, new InvertFilter() },
            { OperationType.NoiseFilter, new NoiseFilter() },
            { OperationType.SepiaFilter, new SepiaFilter() },
            { OperationType.SharpenFilter, new SharpenFilter() },
            { OperationType.TintFilter, new TintFilter() },
            { OperationType.MirrorTransform, new MirrorTransform() }
        };
    }

    public IImageOperation GetOperation(OperationType type)
    {
        return _operations.TryGetValue(type, out IImageOperation operation) ? operation : null;
    }
}