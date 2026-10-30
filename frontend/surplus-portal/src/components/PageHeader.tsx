import type { ReactNode } from "react";

interface PageHeaderProps {
  title: string;
  lead?: string;
  actions?: ReactNode;
}

export default function PageHeader({ title, lead, actions }: PageHeaderProps) {
  return (
    <header className="page-header">
      <div>
        <h1>{title}</h1>
        {lead && <p className="page-lead">{lead}</p>}
      </div>
      {actions}
    </header>
  );
}
