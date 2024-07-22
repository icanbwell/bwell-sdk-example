import { Box, TextField } from '@mui/material';

interface PaginationFormProps {
    page: number;
    pageSize: number;
    onPageChange: (page: number) => void;
    onPageSizeChange: (pageSize: number) => void;
}

const PaginationForm = ({ page, pageSize, onPageChange, onPageSizeChange }: PaginationFormProps) => {
    return (
        <Box sx={{ padding: '5px' }}>
            <TextField
                label="Page"
                type="number"
                value={page}
                onChange={(e) => onPageChange(Number(e.target.value))}
            />
            <TextField
                label="Page Size"
                type="number"
                value={pageSize}
                onChange={(e) => onPageSizeChange(Number(e.target.value))}
            />
        </Box>
    );
};

export default PaginationForm;
