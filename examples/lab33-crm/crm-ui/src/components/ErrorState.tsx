export function ErrorState({
  message,
  onRetry,
}: {
  message: string
  onRetry?: () => void
}) {
  return (
    <div role="alert">
      <p>{message}</p>
      {onRetry && <button onClick={onRetry}>Retry</button>}
    </div>
  )
}
