import { useDispatch, useSelector } from 'react-redux';
import { Box, ToggleButton, ToggleButtonGroup } from '@mui/material';
import { toggleValue, selectToggle } from '@/store/toggleSlice';
import { AppDispatch, RootState } from '@/store/store';

interface ViewToggleProps {
    locator: string;
}

const ViewToggle: React.FC<ViewToggleProps> = ({ locator }) => {
    const dispatch = useDispatch<AppDispatch>();
    const showTable = useSelector((state: RootState) => selectToggle(state, locator));

    const handleToggleView = () => {
        dispatch(toggleValue(locator));
    };

    return (
        <Box sx={{ padding: '5px' }}>
            <ToggleButtonGroup
                value={showTable ? 'table' : 'json'}
                exclusive
                onChange={handleToggleView}
            >
                <ToggleButton value="table">Table</ToggleButton>
                <ToggleButton value="json">JSON</ToggleButton>
            </ToggleButtonGroup>
        </Box>
    );
};

export default ViewToggle;
