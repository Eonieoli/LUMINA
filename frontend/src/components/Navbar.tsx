import ProfileIcon from '../assets/images/navbar/Ic_Pre_profile.png'
import { Link } from 'react-router-dom';
import { HomeIcon, SearchIcon, DonationIcon, RankingIcon } from '@/assets/images';

export default function Navbar() {
  return(
    <nav className="
     /* 모바일뷰 768px까지 */
    fixed bottom-0 w-full bg-white p-2 flex justify-around h-20 border-t border-gray-300 items-center

    /* 웹뷰 768px부터 */
    md:top-0 md:left-0 md:h-full md:w-20 md:flex-col md:justify-around md:items-center md:py-10 md:border-r
    ">

      <Link to="/">
      <img src={HomeIcon} alt="Home" className="w-6 h-6 cursor-pointer transition-transform duration-300 hover:scale-120"/>
      </Link>

      <Link to="/search">
      <img src={SearchIcon} alt="Search" className="w-6 h-6 cursor-pointer transition-transform duration-300 hover:scale-120"/>
      </Link>

      <Link to="/donate">
      <img src={DonationIcon} alt="Donate" className="w-8 h-6 cursor-pointer transition-transform duration-300 hover:scale-120"/>
      </Link>

      <Link to="/ranking">
      <img src={RankingIcon} alt="Ranking" className="w-6 h-6 cursor-pointer transition-transform duration-300 hover:scale-120"/>
      </Link>

      <Link to="/mypage">
      <img src={ProfileIcon} alt="Profile" className="w-6 h-6 cursor-pointer transition-transform duration-300 hover:scale-120"/>
      </Link>
    </nav>
  )
}