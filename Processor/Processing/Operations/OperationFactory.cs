using Shared.Enums;

namespace Processing.Operations;

public class OperationFactory
{
    private readonly Dictionary<OperationType, IImageOperation> _operations;

    public OperationFactory()
    {
        _operations = new Dictionary<OperationType, IImageOperation>()
        {
            { OperationType.DoNothingFilter, new DoNothingFilter() },
            { OperationType.GrayscaleFilter, new GrayscaleFilter() },
            { OperationType.InvertFilter, new InvertFilter() },
            { OperationType.MirrorTransform, new MirrorTransform() }
        };
    }

    public IImageOperation GetOperation(OperationType type)
    {
        return _operations.TryGetValue(type, out IImageOperation operation) ? operation : null;
    }
}