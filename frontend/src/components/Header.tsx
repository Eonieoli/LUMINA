import { BellIcon, LuminaLogo, PlusIcon } from '@/assets/images';
import { Link } from 'react-router-dom';

export default function Header() {
    const scrollToTop = () => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    return (
        <>
            <div className="sticky top-0 z-30 flex justify-between bg-white px-5 pt-12 pb-2">
                <img onClick={scrollToTop} src={LuminaLogo} alt="로고" />
                <div className="flex gap-x-2">
                    <img className="h-6 w-auto" src={BellIcon} alt="종" />
                    <Link to="/post">
                        <img
                            className="h-6 w-auto"
                            src={PlusIcon}
                            alt="더하기"
                        />
                    </Link>
                </div>
            </div>
        </>
    );
}
