interface LoadingBlockProps {
  label?: string;
}

export default function LoadingBlock({ label = "Loading…" }: LoadingBlockProps) {
  return (
    <div className="loading-block" role="status">
      <span className="spinner" aria-hidden />
      {label}
    </div>
  );
}
