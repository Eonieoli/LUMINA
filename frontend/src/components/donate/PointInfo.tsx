import { useEffect, useState } from 'react';
import { CoinIcon } from '@/assets/images';
import { getPointInfo } from '@/apis/donation';

export default function PointInfo() {
    const [point, setPoint] = useState<number>(795);

    useEffect(() => {
        const fetchPointInfo = async () => {
            const response = await getPointInfo();
            setPoint(response.point);
        };
        fetchPointInfo();
    }, []);

    return (
        <div className="flex h-6 items-center justify-end">
            <p className="text-[15px] font-bold mr-2">{point}</p>
            <img src={CoinIcon} alt="coinImg" className="w-5" />
        </div>
    );
}
