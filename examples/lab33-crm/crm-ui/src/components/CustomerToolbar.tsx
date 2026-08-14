export function CustomerToolbar({ onAdd }: { onAdd: () => void }) {
    return (
        <div className="toolbar">
            <button onClick={onAdd}>Add customer</button>
        </div>
    )
}
