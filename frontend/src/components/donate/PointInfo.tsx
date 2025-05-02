import { useEffect, useState } from 'react';
import { CoinIcon } from '@/assets/images';
import { getPointInfo } from '@/apis/donation';

export default function PointInfo() {
    const [point, setPoint] = useState<number>(795);

    useEffect(() => {
        const fetchPointInfo = async () => {
            try {
                const response = await getPointInfo();
                setPoint(response);
            } catch (error) {
                console.error(error);
            }
        };
        fetchPointInfo();
    }, []);

    return (
        <div className="mb-6 flex h-6 items-center justify-end">
            <img src={CoinIcon} alt="coinImg" className="mr-2 w-5" />
            <p className="text-[15px] font-bold">{point}</p>
        </div>
    );
}
