// import { getAllUsers } from "@/apis/admin"
// import { SearchIcon } from "@/assets/images";
// import { useEffect, useState } from "react"

// interface User {
//     userId: number;
//     nickname: string;
//     profileImage: string;
//     message: string;
//     point: number;
//     sumPoint: number;
//     positiveness: number;
// }

// export default function Admin() {
//     const [fetchFirst, setFetchFirst] = useState<boolean>(false);
//     const [pageNum, setPageNum] = useState<number>(1);
//     const [totalPage, setTotalPage] = useState<number>(1);
//     const [users, setUsers] = useState<User[]>([]);


//     useEffect(() => {
//         if (fetchFirst) return;

//         const fetchAllUser = async () => {
//             const response = await getAllUsers(pageNum);
//             setUsers(response.data.users);
//             setTotalPage(response.data.totalPage);
//         }

//         fetchAllUser();
//     }, [fetchFirst, pageNum])
//     return (
//         <div className="flex flex-col justify-center items-center h-dvh w-full bg-pink-200">
//           <div className="flex flex-col justify-evenly bg-white w-full h-full md:w-4/5 md:h-4/5 p-4 rounded-2xl overflow-auto">
//             <h1 className="text-center font-bold text-3xl mb-4">관리자 페이지</h1>
//             <table className="table-auto w-full text-center border-collapse">
//               <thead className="bg-gray-100">
//                 <tr>
//                   <th className="border p-2">ID</th>
//                   <th className="border p-2">프로필</th>
//                   <th className="border p-2">닉네임</th>
//                   <th className="border p-2">포인트</th>
//                   <th className="border p-2">누적 포인트</th>
//                   <th className="border p-2">긍정지수</th>
//                   <th className="border p-2">게시물 조회</th>
//                   <th className="border p-2">댓글 조회</th>
//                   <th className="border p-2">유저 삭제</th>
//                 </tr>
//               </thead>
//               <tbody>
//                 {users.map((user) => (
//                   <tr key={user.userId} className="hover:bg-gray-200">
//                     <td className="border p-2">{user.userId}</td>
//                     <td className="border p-2">
//                       <img className="w-10 h-10 object-cover rounded-full mx-auto" src={user.profileImage} alt="프로필" />
//                     </td>
//                     <td className="border p-2">{user.nickname}</td>
//                     <td className="border p-2">{user.point}</td>
//                     <td className="border p-2">{user.sumPoint}</td>
//                     <td className="border p-2">{user.positiveness}</td>
//                     <td className="border p-2 cursor-pointer">
//                         <div className="flex justify-center">
//                             <img className="w-6 h-6" src={SearchIcon} alt="" />
//                         </div>
//                     </td>
//                     <td className="border p-2 cursor-pointer">
//                         <div className="flex justify-center">
//                             <img className="w-6 h-6" src={SearchIcon} alt="" />
//                         </div>
//                     </td>
//                     <td className="border p-2 font-black hover:bg-red-300 cursor-pointer">✕</td>
//                   </tr>
//                 ))}
//               </tbody>
//             </table>
//           </div>
//         </div>
//       );
      
// }

import { deleteUser, getAllUsers } from "@/apis/admin";
import { SearchIcon } from "@/assets/images";
import { useEffect, useState } from "react";
import { Comments, Posts } from "./adminPages";

interface User {
  userId: number;
  nickname: string;
  profileImage: string;
  message: string;
  point: number;
  sumPoint: number;
  positiveness: number;
}

export default function Admin() {
  const [pageNum, setPageNum] = useState<number>(1);
  const [totalPage, setTotalPage] = useState<number>(1);
  const [users, setUsers] = useState<User[]>([]);

  // 조회하려는 유저
  const [curUserId, setCurUserId] = useState<number>(-1);

  // 유저 게시물 및 댓글
    const [isPostsOpened, setIsPostsOpened] = useState(false);
    const [isCommentsOpened, setIsCommentsOpened] = useState(false);
    const [isPostsAnimation, setIsPostsAnimation] = useState(false);
    const [isCommentsAnimation, setIsCommentsAnimation] = useState(false);

    const [isModalOpened, setIsModalOpened] = useState(false);

    const fetchAllUser = async () => {
      const response = await getAllUsers(pageNum);
      setUsers(response.data.users);
      setTotalPage(response.data.totalPages);
    };
    
  useEffect(() => {
    fetchAllUser();
  }, [pageNum]);

  const handlePageChange = async (newPage: number) => {
    if (newPage >= 1 && newPage <= totalPage) {
      setPageNum(newPage);
    }
  };

  const openModal = (type: string, userId: number) => {
    setCurUserId(userId);
    if (type === 'post') {
        setIsPostsOpened(true);
        setIsPostsAnimation(true);
    } else if (type === 'comment') {
        setIsCommentsOpened(true);
        setIsCommentsAnimation(true);
    } else if (type === 'delete') {
        setIsModalOpened(true);
    }
  }

  const handleClose = (type: string) => {
    setIsPostsOpened(false);
    setIsCommentsOpened(false);
    setTimeout(() => {
        if (type === 'post') {
            setIsPostsAnimation(false);
        } else if (type === 'comment') {
            setIsCommentsAnimation(false);
        } else if (type === 'delete') {
            setIsModalOpened(false);
        }

    }, 400);
  }

  const handleDelete = async () => {
      await deleteUser(curUserId);
      setIsModalOpened(false);
      fetchAllUser();
  }

  return (
    <div className="relative flex flex-col justify-center items-center h-dvh w-full bg-pink-200">
        {isPostsAnimation && <Posts isVisible={isPostsOpened} onClose={() => handleClose('post')} userId={curUserId} />}
        {isCommentsAnimation && <Comments isVisible={isCommentsOpened} onClose={() => handleClose('comment')} userId={curUserId} />}
      <div className="flex flex-col justify-between bg-white w-full h-full md:w-4/5 md:h-4/5 p-4 rounded-2xl overflow-auto">
        <h1 className="text-center font-bold text-3xl mb-4">관리자 페이지</h1>

        {/* 유저 테이블 */}
        <table className="table-auto w-full text-center border-collapse">
          <thead className="bg-gray-100">
            <tr>
              <th className="border p-2">ID</th>
              <th className="border p-2">프로필</th>
              <th className="border p-2">닉네임</th>
              <th className="border p-2">포인트</th>
              <th className="border p-2">누적 포인트</th>
              <th className="border p-2">긍정지수</th>
              <th className="border p-2">게시물 조회</th>
              <th className="border p-2">댓글 조회</th>
              <th className="border p-2">유저 삭제</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.userId} className="hover:bg-gray-200">
                <td className="border p-2">{user.userId}</td>
                <td className="border p-2">
                  <img
                    className="w-10 h-10 object-cover rounded-full mx-auto"
                    src={user.profileImage}
                    alt="프로필"
                  />
                </td>
                <td className="border p-2">{user.nickname}</td>
                <td className="border p-2">{user.point}</td>
                <td className="border p-2">{user.sumPoint}</td>
                <td className="border p-2">{user.positiveness}</td>
                <td onClick={() => openModal("post", user.userId)} className="border p-2 cursor-pointer">
                  <div className="flex justify-center">
                    <img className="w-6 h-6" src={SearchIcon} alt="게시물 조회" />
                  </div>
                </td>
                <td onClick={() => openModal("comment", user.userId)} className="border p-2 cursor-pointer">
                  <div className="flex justify-center">
                    <img className="w-6 h-6" src={SearchIcon} alt="댓글 조회" />
                  </div>
                </td>
                <td onClick={() => openModal("delete", user.userId)} className="border p-2 font-black hover:bg-red-300 cursor-pointer">
                  ✕
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {/* 페이지네이션 버튼 */}
        <div className="flex justify-center items-center gap-2 mt-4">
          <button
            onClick={() => handlePageChange(pageNum - 1)}
            disabled={pageNum === 1}
            className="px-3 py-1 border rounded disabled:opacity-50 cursor-pointer"
          >
            이전
          </button>

          {Array.from({ length: totalPage }, (_, i) => i + 1).map((page) => (
            <button
              key={page}
              onClick={() => handlePageChange(page)}
              className={`px-3 py-1 border rounded cursor-pointer ${
                pageNum === page ? "bg-[#9C97FA] text-white" : ""
              }`}
            >
              {page}
            </button>
          ))}

          <button
            onClick={() => handlePageChange(pageNum + 1)}
            disabled={pageNum === totalPage}
            className="px-3 py-1 border rounded disabled:opacity-50 cursor-pointer"
          >
            다음
          </button>
        </div>
      </div>

      {/* 모달 */}
      {isModalOpened &&
        <div onClick={() => setIsModalOpened(false)} className="absolute flex flex-col justify-center items-center w-full h-full bg-[#00000090]">
            <div onClick={(e) => e.stopPropagation()} className="flex flex-col justify-center items-center gap-y-10 text-2xl font-bold bg-white p-20">
                <p className="font-black text-3xl">정말로 해당 유저를 삭제하시겠습니까?</p>
                <p>삭제 후, 복구 할 수 없습니다.</p>
                <div className="flex justify-center items-center gap-x-4">
                    <button onClick={() => setIsModalOpened(false)} className="bg-gray-200 p-4 rounded-xl hover:bg-gray-400 cursor-pointer">취소</button>
                    <button onClick={() => handleDelete()} className="bg-red-200 p-4 rounded-xl hover:bg-red-500 cursor-pointer">삭제</button>
                </div>
            </div>
        </div>}
    </div>
  );
}
