import { Button, List, ListItem, ListItemText } from "@mui/material";
import type { RecommendedConnection } from "@/sdk/journey";

export default function ConnectList({ items, connectedIds, onConnect, busyId }: {
  items: RecommendedConnection[]; connectedIds: Set<string>; onConnect: (connectionId: string) => void; busyId?: string | null;
}) {
  return (
    <List disablePadding>
      {items.map((item) => (
        <ListItem key={item.id} divider secondaryAction={
          connectedIds.has(item.connectionId)
            ? <span>Connected</span>
            : <Button variant="outlined" size="small" disabled={busyId === item.connectionId} onClick={() => onConnect(item.connectionId)}>Sign in to connect</Button>
        }>
          <ListItemText primary={item.title} />
        </ListItem>
      ))}
    </List>
  );
}
