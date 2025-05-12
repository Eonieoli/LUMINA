import { SearchIconGray } from '@/assets/images';
import React from 'react';

interface DonationSearchProps {
    keyword: string;
    setKeyword: (value: string) => void;
    onSearchClick: () => void;
    autoFocus?: boolean;
}

export default function DonateSearchBar({
    keyword,
    setKeyword,
    onSearchClick,
    autoFocus = false,
}: DonationSearchProps) {
    // 엔터를 쳐도 검색이 가능하도록
    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') {
            onSearchClick();
        }
    };

    return (
        <div className="flex h-9 w-full items-center rounded-3xl border border-gray-300">
            <input
                type="text"
                placeholder="기부처를 검색해보세요."
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                className="ml-4 flex-1 text-[16px] outline-none"
                autoFocus={autoFocus}
                onKeyDown={handleKeyDown}
            />
            <img
                src={SearchIconGray}
                alt="searchIcon"
                className="m-4 w-3 text-gray-300"
                onClick={onSearchClick}
            />
        </div>
    );
}
