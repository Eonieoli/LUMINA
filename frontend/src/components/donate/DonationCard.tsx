import { donationImageMap, defaultDonationThumbnail } from './DonationImageMap';

export interface DonationProps {
    donationId: number;
    donationName: string;
}

export default function DonationCard({
    donationId,
    donationName,
}: DonationProps) {
    // 만약 donationId와 일치하는 썸네일이 없다면 디폴트 이미지로 대체
    const donationImg =
        donationImageMap[donationId] || defaultDonationThumbnail;

    return (
        <div className="flex w-full flex-col items-center justify-center gap-3">
            <div className="flex w-full cursor-pointer items-center justify-center rounded-xl border border-gray-300 p-2">
                <img
                    src={donationImg}
                    alt={`${donationName} 이미지`}
                    className="h-40 w-50 object-contain"
                />
            </div>
            <p className="text-l text-center font-medium text-gray-600">
                {donationName}
            </p>
        </div>
    );
}
