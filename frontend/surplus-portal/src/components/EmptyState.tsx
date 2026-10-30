import type { ReactNode } from "react";
import { Link } from "react-router-dom";

interface EmptyStateProps {
  icon?: string;
  title: string;
  description: string;
  actionLabel?: string;
  actionTo?: string;
  onAction?: () => void;
  children?: ReactNode;
}

export default function EmptyState({
  icon = "📭",
  title,
  description,
  actionLabel,
  actionTo,
  onAction,
  children,
}: EmptyStateProps) {
  return (
    <div className="empty-state">
      <div className="empty-state-icon" aria-hidden>
        {icon}
      </div>
      <h3>{title}</h3>
      <p>{description}</p>
      {actionTo && actionLabel && (
        <Link className="button-link" to={actionTo}>
          {actionLabel}
        </Link>
      )}
      {onAction && actionLabel && !actionTo && (
        <button type="button" onClick={onAction}>
          {actionLabel}
        </button>
      )}
      {children}
    </div>
  );
}
